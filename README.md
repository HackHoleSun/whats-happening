# What's Happening — Novi Sad

An Android app for discovering events happening in Novi Sad, Serbia. The app
shows a feed of concerts, festivals, exhibitions, and other events scraped
from local listings, with filtering by category, date range, and distance,
plus a map view and saved events.

## Project structure

- **`android/`** — Native Android app (Kotlin, Jetpack Compose). The main
  client, built around a Home feed, Search, Map, Saved, and Event Detail
  screens.
- **`scraper/`** — Python scraper that pulls event listings from
  [mojnovisad.com](https://www.mojnovisad.com/desavanja), geocodes venues via
  Nominatim, and writes the result to `scraper/events.json`. This file is
  fetched directly by the app from GitHub and refreshed by CI on a schedule.
- **`worker/`** — Cloudflare Worker that proxies and caches individual event
  detail pages (description + image) for the app's detail screen.

## Android app

Requires Android Studio / a recent Android Gradle Plugin. Open
`android/` as the project root.

```sh
cd android
./gradlew assembleDebug
```

Key architecture notes:

- Screens follow a stateless-Composable + ViewModel pattern (e.g.
  `HomeScreen` + `HomeViewModel`), with `EventRepository` as the shared data
  source (`RemoteEventRepository` in production, `MockEventRepository` for
  previews).
- Events are fetched from `scraper/events.json` (via the GitHub raw URL) and
  cached on-device for 24h.
- Event detail descriptions/images are fetched on-demand from the Cloudflare
  Worker and cached in memory.
- Saved events and user preferences (theme, accent color) persist via
  Jetpack DataStore.

## Scraper

Requires Python 3.12+.

```sh
cd scraper
pip install -e .
python scrape.py
```

This regenerates `events.json` and updates `geocode_cache.json` with any new
venue coordinates. A scheduled GitHub Action commits the refreshed
`events.json` periodically.

## Worker

Requires [Wrangler](https://developers.cloudflare.com/workers/wrangler/).

```sh
cd worker
npm install
npm run dev      # local development
npm run deploy   # deploy to Cloudflare
```

The worker accepts a `?url=` query parameter pointing at a
`www.mojnovisad.com` event page, extracts its description and main image, and
returns them as JSON with edge caching applied.
