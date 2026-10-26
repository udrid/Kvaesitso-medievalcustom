# Handoff: T4-header stage 1

What changed (branch `claude/T4-header`, all in `app/ui/.../ui/medieval/` unless noted, no upstream edits):
- Part 1 design (owner-approved 2026-10-25): `docs/design/T4-header.html/.png`, weather `T4-weather.js`, `T4-weather-A.html/.png` (chosen palette A2 "accentSoft"); option sheets `T4-cap-options*.png`, `T4-weather-colors.png` kept for the record.
- Font: `res/font/unifrakturmaguntia_book.ttf` (SIL OFL 1.1, licence in `docs/licenses/fonts/OFL-unifrakturmaguntia.txt`), `Medieval.Gothic` in `MedievalTheme.kt`.
- `VectorScene.kt` (new): SVG-path primitives, stop-motion frame parts, `VectorCanvas` (frame clock via `withFrameMillis` + 80 ms delay, so it stops when nothing is drawn).
- `PlanetaryDays.kt` (new): weekday -> planet, medallion (ring ornament, disc, alchemical sign) and divider per planet, ported 1:1 from T4-header.html.
- `WeatherScene.kt` (new): 8 scenes, moon phase from the synodic month (no API, no permission), Kvaesitso `Forecast.icon` -> scene (night only changes clear / partly cloudy to the clear-night scene; no forecast = no scene).
- `HeaderSection.kt` rewritten: header at stage y=30; drop cap = 4 text layers (dark offset + red glow, two gold outlines a hair apart standing in for the wobble filter, oxblood gradient); weather line (max 64 wide, ellipsis) and scene under the clock. `StatusLine.kt`: weather removed, row moved to y=128, top line removed (the divider replaces it).
- 8% larger: `MedievalHomeComponent.kt` puts everything below the header in a 361 x 640 body drawn at 1.08 (= 390 wide) from y=150; section coordinates moved to that body (Panel 16/329 wide, pennants, orb, shields, ledger 330, command 404, dispatches 454, dock 551/566). Treasury total 17 -> 16 and 130 wide so it fits (as in the approved mock).

How it was checked:
- Local compile `./gradlew :app:ui:compileDebugKotlin` (JAVA_HOME = Android Studio jbr): BUILD SUCCESSFUL, no warnings in medieval/ files I touched. Full APK not built locally; no GitHub build yet (needs owner OK).
- Drawings ported from the approved HTML; no new `catch`.

NOT verified (workspace cannot run the app):
- Everything on the phone: positions of the Compose text (Compose line boxes differ from CSS, so the drop cap, name and date may sit a few px off), the medallion near "Wednesday", the weather text ellipsis, the scene's legibility at 64 x 26, the dock fitting above the nav bar (body ends at y=841 of 844).
- The drop cap's handpainted wobble is approximated (no displacement filter in Compose).
- Which Kvaesitso icon codes the owner's weather provider really sends; deprecated codes (7, 8, 10, 15, 19) show no scene.
- Battery use of the animation (about 12 redraws per second of a small canvas).
- Moon phase accuracy (simple mean synodic month; can be up to ~1 day off).

Next stage needs: nothing. Possible follow-ups: night versions of rain/cloud/snow/fog scenes; Treasury layout (T2).
