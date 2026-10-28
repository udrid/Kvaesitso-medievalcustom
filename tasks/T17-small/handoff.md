# Handoff: T17-small
Branch `claude/T17-small` (from `claude/T16-polish`). Only `app/ui/.../ui/medieval/` files, no upstream files: `HeaderSection.kt`, `MedievalParts.kt`, `Pennants.kt`.

## 1. New-day shimmer (`HeaderSection.kt`)
- `DropCap(letter, shimmer)` draws, on top of the red fill, a fifth text layer whose brush is a diagonal gold gradient band (0xF3D27A, alpha up to 0.85 with a sine hump) sliding from left to right across the letter. A text brush paints only the letter shape, so nothing spills.
- Trigger: a `LaunchedEffect` keyed on today's date (the clock state, so it also fires at midnight while Home is on screen) and on a resume counter (`LifecycleEventEffect(ON_RESUME)`). It waits one frame so the motion gate has settled after a resume, reads `last_day` in SharedPreferences `medieval_home`, and writes today's date if different. Only when there was an earlier date AND `LocalMotion.ambient` is open it counts up `newDays`; `rememberOneShot(newDays, 1500, ...)` plays it. The very first run ever records the date without a shimmer. Gate closed = date recorded, no shimmer, never replayed. No ambient cost: nothing runs between shimmers.
- Not repeated on later resumes the same day because the stored date equals today.

## 2. Weather line fit (`HeaderSection.kt`, `WeatherLine`)
- Box unchanged (74 dp wide, line height fixed at 14, so the scene below does not move). `TextMeasurer` measures the real text with the real font: first the full text from 12.5 sp down to 10 sp in 0.5 steps; if still too wide, the short form (`shortCondition`) from 12.5 down to 10; last resort short form at 10 sp with the old ellipsis at the END (the temperature is at the start, so it always shows). Clock and scene untouched.
- Short forms (all English condition words of `core/i18n` `weather_condition_*`, 46 strings): Clear sky->Clear, Partly cloudy->Part cloudy, Light rain->Drizzle, Heavy rain(+showers)->Downpour, Rain showers/Light rain showers->Showers, all sleet variants->Sleet, Light snow(+showers)->Flurries, Heavy snow(+showers)->Snowfall, Snow showers->Snow, Thunderstorm and every "...and thunder"->Storm; short words unchanged (Fog, Haze, Hail, Fair, Wind, Cloudy, Rain, Snow, Sleet, Thunder, Unknown).
- Longest result: "Part cloudy" (11 letters) after a temperature prefix of at most 7 characters ("-10° · " / "100° · "), 18 characters. Estimate at 10 sp with an average advance of about 0.36 em for Fell English italic (spaces, "°", "·", "l", "t", "r" are narrow): 18 x 0.36 x 10 = 64.8 dp against 74 dp, about 9 dp spare. The full "Partly cloudy" would be 20 chars: 90 dp at 12.5 sp, about 72 dp at 10 sp, borderline, so it can show whole when it fits, else the short form. The 0.36 em figure is an ESTIMATE; the code measures at run time, so the real fit does not depend on it.
- Non-English texts, OpenWeatherMap's own descriptions and plugin texts have no short form: they are shrunk, then cut with "..." as before.

## 3. Battery leftovers (pollers in `medieval/`)
Found and changed:
- `rememberNow()` (MedievalParts): looped every 5 s forever, even with Home stopped. Now `produceWhileResumed`: runs only while Home is RESUMED, reads at once on resume, then once per minute aligned to the minute change (about 12x fewer wakes while visible). The clock still updates every minute; the `periodMs` parameter is gone.
- `rememberVitals()` (Pennants: battery/storage/memory flags): looped every 5 s forever. Now `produceWhileResumed`, same 5 s while Home is visible, nothing otherwise, refreshes first thing on resume.
- New shared helper `produceWhileResumed` (MedievalParts) = `produceState` + `repeatOnLifecycle(RESUMED)`.
Checked and left alone (already gated or not polls):
- `Ledger` 30 s loop: only while `motion.ambient`. `VectorCanvas` stop motion, `RingLight`, `rememberAmbientClock`, `rememberOneShot`: gated by `LocalMotion` (T12/T9). `PillStatus` (Wi-Fi/signal/Bluetooth/Hushed): callbacks registered ON_START, removed ON_STOP; no polling. `MedievalHomeVM` 60 s: a one-time timeout, not a loop. `CommandLayerContent` people load: runs on a change, not a loop. `HomePressed` collects a flow, no timer.

## Checked
`./gradlew :app:ui:compileDebugKotlin --offline`: BUILD SUCCESSFUL.

## NOT verified (phone only)
- The look of the shimmer (band width, brightness, direction) and that text brush gradient coordinates cover the letter as computed (I assumed its box is about 68 sp wide).
- That the date-change shimmer fires on a real date change and that `LocalMotion.ambient` is already true one frame after resume.
- Real widths of the weather text; which condition texts the owner's provider sends.
- Battery effect (not measured; fewer wakeups by arithmetic only).
