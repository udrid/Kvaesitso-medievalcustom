# Handoff: T4c-weather stage 1

Branch `claude/T4c-weather`. Two code commits: (1) upstream edit in `data/weather/.../WeatherRepository.kt`, (2) `app/ui/.../ui/medieval/`.

Findings (code and one live request)
- Our header collects the same flow as Kvaesitso's widget (`getDailyForecasts`), and the repository fetches without any widget being enabled. No gating on our side.
- Bright Sky DOES serve a US point: live request for San Francisco returned 22 hourly records from DWD source "SAN FRANCISCO" 17 km away (default radius 50 km, so a US city is fine; a rural spot far from a station could return nothing). So no provider change was needed and no owner question.
- Only Bright Sky is offered because MET Norway needs a `metno_contact` string resource (User-Agent policy) that this fork does not define, OpenWeatherMap needs an API key, Breezy needs its app. Adding MET Norway = owner contact email in the APK (privacy/choice; not done).
- Likely cause (code reading, UNVERIFIED on phone): `WeatherUpdateWorker.getLastKnownLocation` waited up to 10 min for a live GPS fix in a background job (no background-location permission => no live updates) and then threw/failed, never reaching the stored-location fallback. Result: no forecast stored.

Changes
- Worker: wait at most 20 s for a fix, then use the cached or stored last location (separate upstream commit).
- `MedievalHomeVM`: `WeatherStatus` (SetUp, AllowLocation, Fetching, Unavailable, Ready). While the launcher is open with permission and auto-location it takes a live fix itself and stores it with `setLastLocation` (this triggers a weather update). "Unavailable" shows after 60 s without a forecast. Logs use tag `medieval`, no coordinates.
- `HeaderSection`: texts "Tap to set up weather" (no provider), "Allow location" (tap asks permission), "Fetching weather…", "Weather unavailable" (tap opens weather settings).

Checked: `./gradlew :app:ui:compileDebugKotlin` green (also compiled :data:weather).

NOT verified
- Everything on the phone, including that the cause above is the real one. If it still fails: `adb logcat -s medieval WeatherUpdateWorker` shows "no location fix" or "Weather update failed".
- "Unavailable" is time-based (60 s), not a real error signal from the worker; a slow first fetch could show it briefly then recover.
- Whether the texts fit the 76 dp column on 2 lines ("Weather unavailable" is the longest).
- Manual city entry path is from the settings code, not seen on screen.
