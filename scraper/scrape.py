import json
import os
import re
from datetime import datetime, timezone, date
from pathlib import Path

import httpx
from bs4 import BeautifulSoup

URL = "https://www.mojnovisad.com/desavanja"
OUTPUT = Path(__file__).parent / "events.json"

GMAPS_KEY = os.getenv("GOOGLE_MAPS_API_KEY")


# ── Geocoding ─────────────────────────────────────────────────────────────────

def geocode(location: str, seen: dict) -> tuple[float | None, float | None]:
    """
    Return (lat, lng) for a venue name.

    [seen] is an in-memory dict that deduplicates calls within a single scraper
    run — if the same venue appears multiple times today (e.g. Arena Cineplex
    has 5 screenings) we only hit the API once.

    Returns (None, None) if the API key is not set or the lookup fails.
    """
    if not location or not GMAPS_KEY:
        return None, None

    if location in seen:
        return seen[location]

    try:
        resp = httpx.get(
            "https://maps.googleapis.com/maps/api/geocode/json",
            params={"address": f"{location}, Novi Sad, Serbia", "key": GMAPS_KEY},
            timeout=10,
        )
        resp.raise_for_status()
        data = resp.json()

        if data.get("results"):
            loc = data["results"][0]["geometry"]["location"]
            result = (loc["lat"], loc["lng"])
        else:
            result = (None, None)

    except Exception as e:
        print(f"  Geocode failed for '{location}': {e}")
        result = (None, None)

    seen[location] = result
    return result


# ── Scraping ──────────────────────────────────────────────────────────────────

def parse_date(header_text: str) -> str | None:
    """Parse 'Danas 23.04.2026.' or 'Sutra 24.04.2026.' to ISO date."""
    match = re.search(r"(\d{1,2})\.(\d{2})\.(\d{4})", header_text)
    if match:
        day, month, year = int(match.group(1)), int(match.group(2)), int(match.group(3))
        return date(year, month, day).isoformat()
    return None


def parse_card(card, current_date: str | None, seen: dict) -> dict | None:
    # URL + slug
    link = card.select_one('a[href*="/navigator/"]')
    if not link:
        return None
    href = link["href"]
    slug_match = re.search(r"/navigator/([^/]+)/", href)
    if not slug_match:
        return None
    slug = slug_match.group(1)

    # Title
    title_tag = card.select_one("h6")
    if not title_tag:
        return None
    title = title_tag.get_text(strip=True)

    # Category
    cat_link = card.select_one('a[href*="/navigator-cat/"]')
    category = cat_link.get_text(strip=True) if cat_link else None

    # Image
    thumb = card.select_one(".single-card__thumb[data-lazybg]")
    image_url = thumb["data-lazybg"] if thumb else None

    # Date / time / location from .single-card__info-details divs
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

    # Coordinates — resolved once per unique location name within this run
    lat, lng = geocode(location, seen) if location else (None, None)

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


def scrape() -> list[dict]:
    resp = httpx.get(URL, follow_redirects=True, timeout=30)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "lxml")

    events = []
    seen_ids: set = set()
    seen_locations: dict = {}  # deduplicates geocode calls within this run

    for wrapper in soup.select("div.date-wrapper"):
        header = wrapper.find("h5")
        current_date = parse_date(header.get_text(strip=True)) if header else None

        for card in wrapper.select("div.events-main__single-card"):
            event = parse_card(card, current_date, seen_locations)
            if event and event["id"] not in seen_ids:
                seen_ids.add(event["id"])
                events.append(event)

    return events


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    if not GMAPS_KEY:
        print("Warning: GOOGLE_MAPS_API_KEY not set — events will have lat/lng = null")

    events = scrape()

    payload = {
        "scraped_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "events": events,
    }
    OUTPUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Scraped {len(events)} events → {OUTPUT}")


if __name__ == "__main__":
    main()
