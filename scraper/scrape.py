import json
import re
import time
from datetime import datetime, timezone, date
from pathlib import Path

import cloudscraper
import httpx
from bs4 import BeautifulSoup

URL = "https://www.mojnovisad.com/desavanja"
OUTPUT = Path(__file__).parent / "events.json"
GEOCODE_CACHE = Path(__file__).parent / "geocode_cache.json"


# ── Geocoding (Nominatim) ─────────────────────────────────────────────────────

SERBIAN_CHARS = str.maketrans("čćšžđČĆŠŽĐ", "ccszd" + "CCSZD")

def slugify(name: str) -> str:
    """Convert a venue name to a stable ASCII slug used as the cache key."""
    name = name.translate(SERBIAN_CHARS)       # č→c, ć→c, š→s, ž→z, đ→d
    name = name.lower()
    name = re.sub(r"[\"'""„]", "", name)       # remove quotes
    name = re.sub(r"[^\w\s-]", " ", name)      # replace non-word chars with space
    name = re.sub(r"[\s_-]+", "-", name)       # collapse spaces/underscores/dashes
    return name.strip("-")

def load_cache() -> dict:
    if GEOCODE_CACHE.exists():
        return json.loads(GEOCODE_CACHE.read_text(encoding="utf-8"))
    return {}


def save_cache(cache: dict) -> None:
    GEOCODE_CACHE.write_text(
        json.dumps(cache, ensure_ascii=False, indent=2), encoding="utf-8"
    )


def geocode(location: str, cache: dict) -> tuple[float | None, float | None]:
    """
    Return (lat, lng) for a venue name.

    Checks the persistent cache first — if the venue was geocoded on any
    previous run it's returned instantly with no API call.

    New venues are looked up via Nominatim using the full name as-is.
    If Nominatim can't find it, the result is stored as null in the cache
    so it can be filled in manually.
    """
    if not location:
        return None, None

    key = slugify(location)

    if key in cache:
        entry = cache[key]
        return entry["lat"], entry["lng"]

    try:
        resp = httpx.get(
            "https://nominatim.openstreetmap.org/search",
            params={"q": f"{location}, Novi Sad, Serbia", "format": "json", "limit": 1},
            headers={"User-Agent": "whats-happening-scraper/1.0"},
            timeout=10,
        )
        resp.raise_for_status()
        results = resp.json()
        result = (float(results[0]["lat"]), float(results[0]["lon"])) if results else (None, None)
    except Exception as e:
        print(f"  Geocode failed for '{location}': {e}")
        result = (None, None)

    time.sleep(1)  # Nominatim rate limit: 1 req/s
    cache[key] = {"lat": result[0], "lng": result[1]}
    return result


# ── Scraping ──────────────────────────────────────────────────────────────────

def parse_date(header_text: str) -> str | None:
    match = re.search(r"(\d{1,2})\.(\d{2})\.(\d{4})", header_text)
    if match:
        day, month, year = int(match.group(1)), int(match.group(2)), int(match.group(3))
        return date(year, month, day).isoformat()
    return None


def parse_card(card, current_date: str | None, cache: dict) -> dict | None:
    link = card.select_one('a[href*="/navigator/"]')
    if not link:
        return None
    href = link["href"]
    slug_match = re.search(r"/navigator/([^/]+)/", href)
    if not slug_match:
        return None
    slug = slug_match.group(1)

    title_tag = card.select_one("h6")
    if not title_tag:
        return None
    title = title_tag.get_text(strip=True)

    cat_link = card.select_one('a[href*="/navigator-cat/"]')
    category = cat_link.get_text(strip=True) if cat_link else None

    thumb = card.select_one(".single-card__thumb[data-lazybg]")
    image_url = thumb["data-lazybg"] if thumb else None

    info_divs = card.select(".single-card__info-details div")
    event_date, event_time, location = current_date, None, None
    for div in info_divs:
        img = div.find("img", src=True)
        if not img:
            continue
        src = img["src"]
        text = div.get_text(strip=True)
        if "calendar" in src:
            event_date = current_date
        elif "clock" in src:
            event_time = text or None
        elif "location" in src or "pin" in src:
            location = text or None

    lat, lng = geocode(location, cache) if location else (None, None)

    return {
        "id": slug,
        "title": title,
        "category": category,
        "date": event_date,
        "time": event_time,
        "location": location,
        "url": href if href.startswith("http") else f"https://www.mojnovisad.com{href}",
        "image_url": image_url,
        "lat": lat,
        "lng": lng,
    }


HEADERS = {
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    "User-Agent": "Mozilla/5.0 (X11; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0",
}


def scrape(cache: dict) -> list[dict]:
    session = cloudscraper.create_scraper()
    resp = session.get(URL, headers=HEADERS, timeout=30)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "lxml")

    wrappers = soup.select("div.date-wrapper")
    if not wrappers:
        # Likely blocked by bot protection — print a snippet to help diagnose
        print(f"[WARN] No date-wrapper elements found. Response snippet:\n{resp.text[:500]}")
        raise RuntimeError("No events found — page structure may have changed or request was blocked")

    events = []
    seen_ids: set = set()

    for wrapper in wrappers:
        header = wrapper.select_one("div.date-separator h2, div.date-separator h3")
        current_date = parse_date(header.get_text(strip=True)) if header else None

        for card in wrapper.select("div.events-main__single-card"):
            event = parse_card(card, current_date, cache)
            if event and event["id"] not in seen_ids:
                seen_ids.add(event["id"])
                events.append(event)

    return events


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    cache = load_cache()
    cache_size_before = len(cache)

    events = scrape(cache)

    new_lookups = len(cache) - cache_size_before
    if new_lookups:
        save_cache(cache)
        print(f"Geocoded {new_lookups} new venue(s), cache now has {len(cache)} entries")
    else:
        print("All venues already cached, no Nominatim calls needed")

    payload = {
        "scraped_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "events": events,
    }
    OUTPUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Scraped {len(events)} events → {OUTPUT}")


if __name__ == "__main__":
    main()
