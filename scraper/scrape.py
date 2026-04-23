import json
import re
from datetime import datetime, timezone, date
from pathlib import Path

import httpx
from bs4 import BeautifulSoup

URL = "https://www.mojnovisad.com/desavanja"
OUTPUT = Path(__file__).parent / "events.json"


def parse_date(header_text: str) -> str | None:
    """Parse 'Danas 23.04.2026.' or 'Sutra 24.04.2026.' to ISO date."""
    match = re.search(r"(\d{1,2})\.(\d{2})\.(\d{4})", header_text)
    if match:
        day, month, year = int(match.group(1)), int(match.group(2)), int(match.group(3))
        return date(year, month, day).isoformat()
    return None


def parse_card(card, current_date: str | None) -> dict | None:
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
            event_date = current_date  # already set from section header; raw display text ignored
        elif "clock" in src:
            event_time = text or None
        elif "location" in src or "pin" in src:
            location = text or None

    return {
        "id": slug,
        "title": title,
        "category": category,
        "date": event_date,
        "time": event_time,
        "location": location,
        "url": href if href.startswith("http") else f"https://www.mojnovisad.com{href}",
    }


def scrape() -> list[dict]:
    resp = httpx.get(URL, follow_redirects=True, timeout=30)
    resp.raise_for_status()
    soup = BeautifulSoup(resp.text, "lxml")

    events = []
    seen_ids = set()

    for wrapper in soup.select("div.date-wrapper"):
        header = wrapper.find("h5")
        current_date = parse_date(header.get_text(strip=True)) if header else None

        for card in wrapper.select("div.events-main__single-card"):
            event = parse_card(card, current_date)
            if event and event["id"] not in seen_ids:
                seen_ids.add(event["id"])
                events.append(event)

    return events


def main():
    events = scrape()
    payload = {
        "scraped_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "events": events,
    }
    OUTPUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2))
    print(f"Scraped {len(events)} events → {OUTPUT}")


if __name__ == "__main__":
    main()
