# R1 final check of S2-screens (`claude/s2-screens-1156ca`, HEAD acac1a934)

## VERDICT: FIX FIRST
Two blockers seen on screen: (1) in the search results every red ribbon is cut in half and the gold frame line runs through it, which is exactly what the owner asked to avoid; (2) a dialog with long text pushes its buttons off the screen. Fixes are small and inside S2's own files; route to the S2 thread. Re-check only those after the fix.

Method: `.claude/agents/final-check.md`. The S2 branch was NOT edited. A throwaway detached copy of it (scratchpad, deleted after) got the test-only screenshot setup from `ops/emulator.md` plus a test that renders S2's shared pieces under S2's theme (`tasks/R1-review/s2-test/S2Shots.kt.txt`, kept for the fix thread). Pictures: `docs/design/shots/S2/`. Paths below are under `app/ui/src/main/java/de/mm20/launcher2/ui/`, line numbers in the S2 branch.

## Top three
1. BLOCKER: result-group ribbons cut and crossed by the gold line (`results-portrait.png`, `results-opened-card.png`).
2. BLOCKER: framed dialog cannot scroll; buttons lost with long text or large font (`dialog-long-small-font-130.png`).
3. SHOULD-FIX: bottom-sheet content no longer fills the sheet width (code, not rendered).

## Findings
### 1. BLOCKER: ribbons on search result groups (owner's ask fails)
`launcher/search/common/list/ListResults.kt:157-192`. Seen: "APPS", "CONTACTS", "CALENDAR" ribbons show only their lower half and the frame's top gold lines run across them. Two causes in the item `Box`: `.shadow(elevation, shape, clip = true)` sits on the Box itself, so it clips the Box's children (the ribbon at y = -8) to the padded area; and `.medievalFrame(...)` is a `drawWithContent` on the same Box, so its lines are drawn AFTER the children, over the ribbon.
Fix: do what `PreferenceCategory` already does (it renders correctly, `settings-portrait.png`): keep frame, padding, shadow and background on the inner `Column` (or a Box around it), and make the `Ribbon` a sibling drawn after it, outside the clipped/framed node: `Box(Modifier.animateItem().fillMaxWidth()) { Column(Modifier.medievalFrame(...).padding(top = topPadding, bottom = bottomPadding).shadow(...).background(...)) {...}; if (showRibbon) Ribbon(ribbon, Modifier.offset(x = 12.dp, y = topPadding - 8.dp)) }`. Also give the first group 8 dp top room (item 6 below) so the list edge does not cut it. Who: S2 thread.

### 2. BLOCKER: dialog buttons pushed off screen
`medieval/DeliveranceDialog.kt:53-57`. Seen at 360 x 640 dp with font 130%: the text runs to the bottom, no Cancel / Hide. Material's AlertDialog gives `text` a `weight(1f, fill = false)` scrolling slot; here `text()` is a plain child. Real cases: long explanations, any dialog at large font, text-field dialogs (TextPreference, rename) when the keyboard opens.
Fix: `Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { text() }`. Short dialogs look right (`dialog.png`). Who: S2 thread.

### 3. SHOULD-FIX: bottom-sheet content loses full width
`component/BottomSheet.kt:155`. Before, `content()` was the direct child of a `Surface`, whose box passes the minimum width to its child; now it is inside a `Column(horizontalAlignment = CenterHorizontally)`, which resets it to 0, so a sheet whose root has no `fillMaxWidth()` shrinks and centres (users of this `BottomSheet`: RestoreBackupSheet, CreateBackupSheet, EditTagSheet, EditSearchActionSheet, ...). Not rendered (the sheet needs the overlay host). Confidence: high from the code.
Fix: `Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) { content(...) }` under `PennantHandle()`. `DismissableBottomSheet.kt` is fine.

### 4. SHOULD-FIX: long ribbon titles run off the panel and the screen
`medieval/DeliveranceTheme.kt:210-231` (`Ribbon`). Seen in `settings-small-360x640.png` and `settings-font-130.png`: a long group title runs past the panel's right edge and off the screen, cut without "...". English titles are short, but other languages and large font hit it.
Fix: give `Ribbon` a max width (e.g. `Modifier.widthIn(max = ...)` from the caller: panel width minus 24 dp) and `overflow = TextOverflow.Ellipsis` on its Text.

### 5. SHOULD-FIX: wax-seal switch tap area is 24 dp tall
`medieval/WaxSwitch.kt:43`. The toggle area is the drawn 46 x 24 dp; Material reserves 48 dp. In `PreferenceWithSwitch` the switch is its own tap target. Looks right on screen (`card-switches.png`, `settings-portrait.png`).
Fix: `.minimumInteractiveComponentSize()` before `.size(46.dp, 24.dp)`.

### 6. NIT: first result group's ribbon at the list top edge
`ListResults.kt:189`. Even after fix 1, the first item has 0 top padding, so the ribbon (8 dp above it) is cut by the list's top edge. Fix: 8 dp extra top padding on the item that shows the ribbon.

### 7. NIT: transparent cards keep the gold frame
`component/LauncherCard.kt:30`. Seen in `card-switches.png`: with background opacity 0 the panel disappears but the double frame stays, so a "transparent" search bar or widget shows a bare frame. Fix: draw `medievalFrame()` only when `backgroundOpacity > 0f`, or ASK OWNER whether transparent should keep the frame. (No shadow smear seen on the opaque card, so that earlier worry is dropped.)

### 8. NIT: dark seams between result rows
`ListResults.kt` item padding: the gap between rows of one group shows the dark ground as a thin dark stripe across the panel (`results-portrait.png`). Acceptable as row dividers; if the owner wants one solid panel, paint the gap with the panel colour.

### 9. ASK OWNER: rounded slider in a sharp-cornered look
The settings slider is Material's rounded expressive slider in gold (`settings-portrait.png`); the wax-seal switch is round too (a seal is round, approved). Question for the owner: A) keep the rounded slider (recommended, cheap); B) a square gold bar with a small seal as the handle (custom work).

### 10. LOW: Appearance theme pickers do nothing now
`theme/LauncherTheme.kt:67-75`: colours, shapes and fonts are fixed and always dark, so the colour scheme, shape, typography and light/dark settings change nothing (S2's handoff says so). Since the owner's colour-scheme decision (decisions.md, 2026-10-28: ONE setting that recolours the whole look), `Deliverance.colorScheme` should become a function of that future setting rather than a fixed value; no change needed now, but do not build on it being constant. ASK OWNER: A) hide the old pickers until the new colour setting exists (recommended); B) leave them.

Note: during this check the S2 branch moved from acac1a934 to 3702e3c ("Merge main into S2-screens": final-check agent, CLAUDE.md line, decisions). No code changed, so the verdict stands.

### 11. NIT: `PennantHandle` has no accessibility label
`component/DismissableBottomSheet.kt:271`. Material's drag handle has expand/collapse semantics for TalkBack. Fix: `Modifier.semantics { contentDescription = "Drag handle" }`.

## Checks done
- SCOPE: only the brief's areas; upstream edits in their own commits (the last code commit mixes `DeliveranceTheme.kt` with the upstream result files: nit); no manifest, permission, dependency, secret or personal data; no copied game art or text; no new `catch`. Note: `git diff main..S2` (two dots) shows main's newer `final-check.md` and a CLAUDE.md line as deletions; S2 did not touch them (three-dot diff is clean).
- CODE: switch slides once (120 ms), nothing loops; grain/vignette static. Vignette over settings text is mild in the render (bottom rows a little darker); readable.
- BUILD: `:app:ui:compileDebugKotlin` and the screenshot tests pass on the S2 copy. Lint not run.
- SEE IT: settings (portrait, 360 x 640, font 130%), result groups (normal, opened card, small + font 130%), framed card, switches on/off/disabled, text button, dialog short and long. Home and the typing layer under S2's theme: identical to before (`s2theme-home-portrait*.png` vs `docs/design/shots/home-portrait.png`). Sharp corners: cards, panels, dialog, list groups all square; only switch, slider and Home's own chips are round.
- NOT seen (CANNOT TEST HERE): the real search page with real apps/contacts, bottom sheets (overlay host), drop-down menus and snackbars, the widget page and add-widget flow, the app picker, the Assistant, scrolling smoothness, landscape (S2 is cut from main, whose Home has no landscape layout yet), anything on the S20 FE.
- TESTLIST (`tasks/S2-screens/testlist.md`): plain words, visible result first, has a should-NOT step and a restart step. Step 3 has more than three do-lines (split it); add "open a long dialog at large font" once fix 2 lands.
