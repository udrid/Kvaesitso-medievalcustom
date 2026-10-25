# Handoff: T1-home stage 1

What changed:
- New package `app/ui/.../ui/medieval/` holds the whole C5 Home: theme + fonts (`MedievalTheme.kt`), grain/vignette, `MedievalHomeComponent` (draws C5 on a 390x844 stage scaled to the screen), header, status line, Treasury (placeholders), pennants, orb, shields, ledger, command bar, dock. Fonts and licences: `app/ui/src/main/res/font/`, `docs/licenses/fonts/`.
- Upstream edits (small, own commits): `SharedLauncherActivity.kt` (Home = `MedievalHomeComponent`; search bar style forced Hidden; status/nav icons forced light); `app_name` = Deliverance in `core/base` main/debug/nightly strings. applicationId unchanged.
- Real data: clock/date, weather (Kvaesitso's weather repository, shows `[weather]` if none set up), Bluetooth (`Settings.Global bluetooth_on`), Hushed (ringer mode), battery %, battery temperature (shown in F when locale is US), free storage %, free RAM %, next two calendar events (Kvaesitso CalendarRepository + its permission flow).
- Choices I made inside the brief (owner can change): Stores = FREE storage %, Wits = FREE memory %, Heat gold fill spans 20-50 C. Shield glyphs: C5 line glyph for the default app, the app's own icon after the owner picks another. Ledger shows the weekday instead of a time when the event is not today.
- Shields: defaults X, Discord, YouTube, Samsung Messages if installed; long-press (or tap an empty one) opens a picker; picks saved in SharedPreferences `medieval_home` (`shield_0..3`). Badges are hidden (T3).
- Orb launches package `com.anthropic.claude`. Command bar tap = `LauncherScaffoldState.onSearchBarTap()` (launcher search).
- SUMMON is NOT built: no AF entry says the Claude app accepts text. Dispatches panel, camera pill not built (out of scope).

How it was checked:
- GitHub Actions build for `claude/T1-home` green (run 40215998780, last commit "9b"). An earlier run failed on a missing import in `CommandBar.kt` and was fixed. No local compile.
- No new empty `catch`; every caught error is logged with tag `medieval`.

NOT verified (workspace cannot run the app):
- How it looks and whether anything overlaps or is cut off on the S20 FE; fonts loading (Cinzel is a variable font set to weight 500/700); ring text length fitting the circle and the U+2720 cross glyph; grain strength.
- `com.anthropic.claude` really is the Claude app package (UNVERIFIED, no AF entry).
- Weather needs a provider + location permission set in the launcher settings; not tested.
- Calendar permission prompt and event times; search opening from the command bar; dock intents (dialer, camera, default browser, Settings) on One UI.
- Bluetooth/Hushed update delay (polled every 5 s); battery temperature/RAM numbers vs. Samsung's own.
- Wi-Fi call and Voicemail are dim placeholders (no simple permission-free API).
- Swipe gestures of upstream Kvaesitso (swipe up = app search, etc.) were left as they were.

Next stage needs:
- T2 Treasury: replace the placeholders in `TreasuryPanel.kt`.
- T3: notification access for shield badges (`Shields.kt` has the comment where the badge goes) and the Dispatches panel (spot at y=612..702 on the stage is empty).
- Camera pill overlay (own task).
- SUMMON: needs an AF entry first (does the Claude app take shared text via ACTION_SEND?).
