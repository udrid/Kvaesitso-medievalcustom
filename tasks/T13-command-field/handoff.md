# Handoff: T13-command-field stage 1

Branch `claude/T13-command-field` (cut from `claude/integrate-1`).

What changed (all new/fork code in `app/ui/.../ui/medieval/`, no upstream file touched)
- `CommandBar.kt`: the idle Home bar is a tap target that calls `onOpen`; its SUMMON sends `SearchVM.searchQuery` (empty = T6 hint toast only). No longer opens the search page.
- New `CommandOverlay.kt`: full-screen layer over Home (fade 120 ms) with a real `BasicTextField` in the C5 bar look (quill, italic placeholder, gold cursor, clear cross, SUMMON). Focus and keyboard are requested on open. Text = `SearchVM.searchQuery` (set through `searchVM.search(it)`), so the swipe-opened search page shows the same string. Results = the existing `SearchColumn` below the bar, only while the text is non-empty (nothing before typing; T11 already removed the all-apps list and favourites for an empty query). Bottom padding = keyboard/nav bar height.
- `MedievalHomeComponent.kt`: `commandOpen` state, overlay placed under the pill/grain, bar tap opens it.
- Close: Back (`BackHandler`), or leaving the launcher (ON_STOP) which calls `searchVM.reset()`; SUMMON sets a 2 s flag so the text survives the trip to the Claude app. Enter key = launch best match (upstream `launchBestMatchOrAction`).

Why an overlay, not an inline field on Home: the Home bar sits mid-screen (stage y 404..448); the keyboard would leave only a sliver for results, so the bar moves to the top while typing. One field instance only (no focus handover between two fields).

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL.

NOT verified (workspace cannot run the app)
- Everything on the phone: focus + keyboard really open on tap (`requestFocus` + `keyboard.show()`), cursor visible, results appear and scroll, Back order (BackHandler vs the scaffold's PredictiveBackHandler), ON_STOP clearing, status bar/pill overlap at the top (top padding = status bar inset + 8 dp).
- Look of the overlay bar vs. the Home bar, and result cards (they use the launcher's normal Material look, as on the search page).
- `SearchColumn` has an internal `search("")` when a private-space profile changes state: could clear the text (rare).
- SUMMON text reaching the Claude app (T6, still UNVERIFIED on phone).
- Gestures that open the search page (swipe) are untouched and not tested with text typed in the bar.
