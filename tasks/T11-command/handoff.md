# Handoff: T11-command stage 1

Branch `claude/T11-command` (cut from `claude/integrate-1`). Root causes found in code:
- Keyboard: `SearchComponent(openKeyboard = searchBarAutofocus)` uses the upstream setting "autofocus search" (default off), so the search page opened without focus.
- All-apps list: with an empty query `SearchColumn` shows apps when the upstream setting `searchUiSettings.allApps` is on (default on), plus pinned favorites.

Changes (upstream edits are tiny, each its own commit)
- `SharedLauncherActivity.kt`: both `SearchComponent(...)` get `openKeyboard = true`.
- `SearchColumn.kt`: `allAppsEnabled = false`; `hideFavs` also true while the query is empty. Nothing shows before typing, no hint added.
- `CommandBar.kt` (new code): SUMMON with empty text now also opens the search (plus the T6 toast). The Home bar is NOT a text field on Home itself: the upstream search page cannot be embedded without a big rewrite, so the bar opens the search page with keyboard up.
- Not touched: SUMMON text path (T6, sends `SearchVM.searchQuery`), Back handling (upstream `reset()` clears the query when the search closes, so old text should not linger).

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL.

NOT verified (workspace cannot run the app)
- Everything on the phone: keyboard and cursor really appear; no list before typing; Back clears; SUMMON sends the typed text and not old text.
- Whether the search box is visible/styled well on the search page (bar style is forced Hidden for Home; it should become visible when search opens, as it did before).
- The assistant mode (`LauncherActivityMode.Assistant`) also got `openKeyboard = true` and the same no-list search; not tested.
- The upstream settings (all apps, autofocus, favorites) are now ignored by this fork's search; their switches in Settings do nothing.
