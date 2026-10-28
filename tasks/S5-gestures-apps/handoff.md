# Handoff: S5-gestures-apps Part 2 (build)

Branch claude/s5-gestures-apps-47305e (merged integrate-5 first). Owner picks: layout C, Today strip as the one extra, combined categories, long-press "Move to category", swipe down off, swipe up = apps page, left/right/double tap/long press = nothing, Home button unchanged.

New code in `medieval/`: `AppsHub.kt` (hub UI, `AppsHubHost` wires real data), `AppsHubData.kt` (categories, shipped package list, rules, `HubPrefs`), `GestureDefaults.kt` (one-time defaults).
Small edits to our files: `CommandLayerContent.kt` (`AppCell` now internal, takes a label and long-press), `Ledger.kt` (`timeLabel` internal), `HeaderSection.kt` (`weatherTemp` internal), `MedievalHomeVM.kt` (calls GestureDefaults once).
Upstream edits, one commit each: `SharedLauncherActivity.kt` (`swipeDown = null`), `WidgetsComponent.kt` (hub above WidgetColumn for the default widgets page), `GestureSettingsScreen.kt` (no Swipe down row), `LauncherSettingsData.kt` (new-install defaults swipe down and double tap = No action).

Rules for category: own move, shipped list (about 150 packages I know; others fall through), Android category, system app = System, else Other.
Deviations: "tag" = my own SharedPreferences override (`hub_cat_<package>`), not a Kvaesitso tag, so it does not show in Kvaesitso's tag list; favourites = own stars (`hub_favourites`), else the launcher's most-used apps. Defaults for what the owner did not choose: one category open at a time, filter on the page, placeholder "Search your apps...".
Pictures: `docs/design/shots/S5/` (test `S5Shots.kt`): collapsed, expanded, filter, menu, move menu, favourites, Ember, landscape. I looked at them.
Checked: compileDebugKotlin and S5Shots (rules test passes).
NOT verified: everything on the phone; real installed apps and how many land in Other; Android's own category values on Samsung apps; the shipped package names are from memory (unlisted ones just fall to Other); whether the saved swipe-up on the phone is the default widgets page (the one-time default writes it); Dropdown menu look inside the real theme (pictures use the Deliverance scheme); long-press menu is tall (Material row height); swipe-up scroll feel with a long widget list below; the one-time gesture write (DataStore update) never ran on a device; Accessibility service no longer matters since double tap/lock are off.
