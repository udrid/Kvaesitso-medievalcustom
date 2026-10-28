# Handoff: S6-landscape-fill stage 2 (Part 2 built, option D)

Branch `claude/s6-landscape-fill-a370fa` (merged `claude/integrate-5` first). Nothing pushed, no build started.

What changed (all in `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/`; portrait code paths untouched)
- `MedievalLandscape.kt`: new `Sec.Forecast`; landscape rectangles for layout D (side columns 232 wide; Ledger 132 high, Dispatches 150, Treasury 75, flags 80, Forecast strip 280 x 58 at y 212). Portrait rects only gain an invisible Forecast entry (so the glide has a start).
- `MedievalHomeComponent.kt`: `Place` takes a layout width and draws the section at rect width / layout width (222 -> 232 is 1.045; Treasury 205 -> 232 is 1.13). Forecast section added to the landscape layout only.
- `Ledger.kt` (4 events, panel 126 high), `Dispatches.kt` (5 rows, panel 143 high) in landscape only. `TreasuryPanel.kt`: text widths follow the column width. `Pennants.kt`: landscape flags 80 high (portrait 62), bigger number; row drawn at 0.94.
- New `ForecastStrip.kt` (Panel + six cells: hour, line icon, temperature) and `MedievalHomeVM.hourly` (hourly forecasts from the weather repository the header already reads; no new fetch, permission or animation). Fewer than six hours are shown as they are; none gives a plain-words line (weather status text); numbers are never invented.
- Tests: `S6Shots.kt` (+ `fakeHomeFull` busy state in `MedievalShots.kt`); pictures in `docs/design/shots/S6/`.

Deviations from the preview
- Flags are taller but not wider (four flags already fill the 232 width); the preview drew them overlapping.
- Right column x is 528 (preview showed the same); the shield labels MESSAGES / DISCORD reach near it: see pictures.

Verified: `./gradlew :app:ui:compileDebugKotlin --offline` and the Robolectric pictures (looked at, see check notes).
NOT verified
- Anything on the phone. Pictures use fake insets, fake data and frozen animation.
- That the real weather provider returns six hours ahead on the phone (Bright Sky is asked for 14 days of hourly data; that is read from code, not seen).
- Large-font and 640 x 360 landscape fit is judged from pictures only.
- The turn glide with the new Forecast section (portrait -> landscape) and taps in the taller panels.

Pictures looked at (`docs/design/shots/S6/`): both turns (Gold, Ember), busy and quiet states, 640 x 360, large font 1.3, portrait full and small. No overlaps found after moving Treasury, the flags and Dispatches down 6 to 8 units (Dispatches ribbon first touched the command bar). Quiet state shows "No hours ahead" (fake VM has no hours) and unchanged panel heights. Portrait pictures match the old layout.
Known gap: a landscape typing-layer picture could not be made (Robolectric ClassCastException in `CommandLayerContent.PeopleAndSwitches` with the relaxed fake view model; not in S6 code). The typing layer is untouched by S6.
