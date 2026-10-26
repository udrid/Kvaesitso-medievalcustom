# Handoff: T5-pill stage 1

Design (owner-approved in chat 2026-10-26 after five rounds): `docs/design/T5-pill-v5.html/.png` (v1-v4 kept for the record). Changes from the first brief, all owner choices: no alarm, charging, Ready or battery; no clock in the pill (the header clock stays); the pill now carries the system status icons instead. `ops/decisions.md` needs a line for this (coordinator only).

What changed (branch `claude/T5-pill`, new files in `app/ui/.../ui/medieval/`):
- `MedievalPill.kt`: pill that hugs the camera hole (cutout bounds from `ViewCompat.getRootWindowInsets(view).displayCutout.boundingRects`, top-centre fallback). Left of the hole: icons of waiting notifications (their own small icons tinted gold, newest first, one per app, max 4, then "+n"). Right: Hushed (moon), Bluetooth, Wi-Fi, signal bars, each only while it applies. Width follows the icons; the hole never moves. No pointer handling. `HideStatusBarWhileHome()` hides the system status bar while Home is composed and resumed, and shows it again when it leaves.
- `PillStatus.kt`: listeners that exist only between ON_START and ON_STOP: default-network callback (Wi-Fi), `TelephonyCallback.SignalStrengthsListener` (API 31+, bars = `SignalStrength.level`), ContentObserver on `Settings.Global bluetooth_on`, receiver for ringer mode and Do Not Disturb changes. No polling, no timers, no new manifest permission.
- `MedievalHomeComponent.kt`: pill + hide call added; the stage's top padding now uses `statusBarsIgnoringVisibility`, so the header does not move when the bar is hidden. `StatusLine.kt` (old Bluetooth/Hushed row, 5 s polling) deleted; its call removed. No upstream file touched.
- `MedievalIcons.kt`: `Moon`.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL, no errors. Nothing pushed, no GitHub build.

NOT verified (workspace cannot run the app):
- Everything on the phone: pill position and size around the real hole (hole size is taken from the cutout rect, pill height = max(26 dp, hole + 10 dp)), legibility at 8 to 11 dp.
- Signal bars: whether `registerTelephonyCallback` needs READ_PHONE_STATE on this phone. If not allowed it logs "Pill: signal bars need a permission we do not hold" (tag `medieval`) and the bars simply do not show. Not tested.
- Hiding the bar: the scaffold's own effect (`LauncherScaffold.kt` ~1128) calls `show(statusBars)` on resume; mine runs later in composition order and hides, but the order is not proven. If the bar stays, the fix is one line: `showStatusBar = false` in `SharedLauncherActivity.kt` (upstream edit, hides it on every launcher screen).
- Whether the bar returns on other launcher screens (search) and whether the top-edge pull-down still opens the quick panel.
- Notification icons: `Icon.loadDrawable` for other apps' small icons, and that they are tinted properly; shown only with notification access.
- Wi-Fi icon shows only when Wi-Fi is the default network (not for mobile data).
- Hushed also lights for Do Not Disturb, which the old row did not (owner asked only for "Hushed"; say if not wanted).
