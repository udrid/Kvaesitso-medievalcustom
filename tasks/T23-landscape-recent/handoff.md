# Handoff: T23-landscape-recent

Branch `claude/t23-landscape-recent-438638` (cut from `claude/t22-lock-wallpaper-5cf90f` = integrate-8). Committed locally, not pushed, no build.

Changes (all in `app/ui/.../ui/medieval/`; portrait untouched)
- `RecentApps.kt` (new): "Recently used" Panel (ribbon, double gold frame, palette/corner aware through the usual Panel), 5 small shields (`ShieldEmblem`, letter fallback) with names, tap = `tapReaction` + `launchSafely` (120 ms rule). Empty state: "Open an app and it will appear here". One row only: no room for a second (the counter keeps 8, shows 5).
- `CommandLayerContent.kt` `UsageCounter`: `bump` also writes `t:<package>` (Long, last launch time) into the same `medieval_usage` prefs; new `recent()` sorts by it. `top()` ignores Long values, so most-used is unchanged.
- `MedievalLandscape.kt`: `Sec.Recent` (landscape rect 0,310,232,62; invisible portrait rect for the glide); pennants moved up 5 units (238 -> 233). Treasury and pennants not shrunk. `MedievalHomeComponent.kt`: one `Place` line. `MedievalWelcome.kt`: Recent rises with the pennants.
- Test `T23Shots.kt`, pictures `docs/design/shots/T23/` (both turns, Gold/Ember, 0/3/8 apps, long name, 640x360, portrait).

Which launches count: anything through `launchSafely` with a package: shields, dock, apps page, typing layer, Recently used itself. Search-result launches via upstream code do NOT go through it, so they are not listed (they only feed the other "Most used" ranking). Apps opened outside the launcher never count; no Usage Access.

NOT verified: anything on the phone; real app names and emblems; that launches from the apps page count (read from code: uses AppCell/launchSafely); the 120 ms flash on the small shields; Android 13 package visibility for `getLaunchIntentForPackage` (shields already rely on it). Pictures used fake apps and frozen animation. Large font (1.3) not rendered.
