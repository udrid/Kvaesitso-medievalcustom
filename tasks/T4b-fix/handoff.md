# Handoff: T4b-fix stage 1

Branch `claude/T4b-fix`, one code commit (app/ui/.../ui/medieval/, no upstream edits).

Root causes
- Layout: `MedievalHomeComponent` ignored the system bars (the launcher draws edge to edge; the `insets` argument only holds the display cutout). The 390x844 stage was centred in the full screen height, so on 411x914 dp its bottom (about y=901) ran under the 3-button bar (about 48 dp). Pennants: x 18/99/180/261 + 58 wide ended at 319 while the panel spans 16..345, so the row was 26 short on the right.
- Weather: no provider/location set (default long-press action is NoAction, so launcher settings were unreachable from Home).

Changes
- `MedievalHomeComponent.kt`: stage is padded by the navigation-bar height at the bottom and by (status bar - 20 dp) at the top; scale = min(width/390, freeHeight/842). StageHeight 844 -> 842 (content ends about y=838).
- `Pennants.kt`: `PennantX` = 14 + i*91.67 so the outer visible flag edges are 16 and 345 and gaps are equal.
- `Dock.kt`: four equal-weight slots instead of SpaceAround (uneven with different label widths).
- `HeaderSection.kt`: long-press the header opens launcher settings (owner choice 2026-10-26); with no forecast the weather spot reads "Tap to set up weather" (2 lines, 10 px italic) and opens settings > weather (`SettingsActivity.ROUTE_WEATHER_INTEGRATION`). `settingsIntent()` helper.
- Kept 1.08 body growth; it did not need reducing for width. The achieved overall scale is height-limited on a 3-button phone: 411x914, nav 48, status 24: free 862 dp -> 1.024 (width would allow 1.054), content 399 dp wide, 6 dp spare each side. Gesture nav (about 24 dp): free 886 -> 1.052. 360x640: free 588 -> 0.698 (text about 30% smaller than design; fits, small).

Checked: `./gradlew :app:ui:compileDebugKotlin` BUILD SUCCESSFUL.

NOT verified (workspace cannot run the app)
- Everything on the phone: that `WindowInsets.navigationBars/statusBars` return the real bar sizes inside this component (if they return 0 the dock stays under the buttons: report it), real dock clearance, pennant alignment.
- That "Tap to set up weather" fits in two lines beside the medallion; that the header long-press does not clash with the scaffold's own gestures.
- That Bright Sky / MET Norway / auto-location works on this phone (steps in testlist B follow the settings code, not tested). The weather line also shows the hint while a first forecast is still loading (null forecast).
- The setting labels "Integrations > Weather" are from the code route, not seen on screen.
- api-scout was not asked; the provider list was read directly from `WeatherRepository.getProviders` (Bright Sky, MET Norway always; OpenWeatherMap/Breezy if available).
