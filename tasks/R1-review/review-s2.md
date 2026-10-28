# R1 review of S2-screens (`claude/s2-screens-1156ca`, read-only)

Diff read: `git diff main...claude/s2-screens-1156ca` (three dots: only S2's own commits; two dots also shows main's newer commits as S2 "deletions" of `final-check.md` and a CLAUDE.md line, which S2 did NOT do). Compiles per S2's handoff. NOT rendered: the emulator cannot run on this PC (`ops/emulator.md`); S2's screens need their own screenshot tests (fake view models) before the next build. The "Typography.kt change" in the brief is only the AlertDialog import swap in `settings/typography/TypographySettingsScreen.kt`; the font change itself is `Deliverance.typography` in `LauncherTheme.kt`.

Paths are under `app/ui/src/main/java/de/mm20/launcher2/ui/`; line numbers are in the S2 branch. Ranked by how likely each costs a rebuild.

## 1. HIGH (likely visible): bottom-sheet content no longer fills the sheet width
`component/BottomSheet.kt:155`. Before, `content()` sat directly in a `Surface`, and Surface's inner box passes its minimum width on to its child (`propagateMinConstraints = true`), so sheet content was always full width. Now it sits in a `Column(horizontalAlignment = CenterHorizontally)`, which resets the minimum width to 0: any sheet whose root does not say `fillMaxWidth()` shrinks to its text width and is centred (RestoreBackupSheet, EditTagSheet, EditSearchActionSheet, CreateBackupSheet, ... use this `BottomSheet`).
Fix: wrap the call in a box that passes the width on: `Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) { content(...) }` inside the Column (keep `PennantHandle()` above it). `DismissableBottomSheet.kt` is fine (it already had a Column with a Box around content).

## 2. HIGH (wrong behaviour): framed AlertDialog cannot scroll, buttons can be pushed off screen
`medieval/DeliveranceDialog.kt:53-57`. Material's AlertDialog puts `text` in a `weight(1f, fill = false)` slot so long content scrolls and the buttons stay visible. Here `text()` is a plain child of the Column: a long text, a long list, or the keyboard opening for a text field (TextPreference, rename dialogs) pushes the button row below the dialog's bottom where it cannot be reached.
Fix: `Box(Modifier.weight(1f, fill = false)) { text() }` (the Column is already height-bounded by the Dialog), and for plain text add `.verticalScroll(rememberScrollState())` on that box.

## 3. MEDIUM (touch target): wax-seal switch is 24 dp tall to tap
`medieval/WaxSwitch.kt:43`. The `toggleable` area is the drawn 46 x 24 dp. Material's Switch reserves a 48 dp touch target (`minimumInteractiveComponentSize`). In `PreferenceWithSwitch.kt` the switch is a separate tap target beside the row text, so it is easy to miss with a thumb.
Fix: put `.minimumInteractiveComponentSize()` before `.size(width = 46.dp, height = 24.dp)` (looks the same, taps a 48 dp square).

## 4. MEDIUM (contrast): grain and vignette over every settings list
`component/preferences/PreferenceScreen.kt:108`. On Home the vignette darkens a fixed picture; here it lies over text. The radial gradient reaches 72% black at its rim: about 40% darker at the bottom centre and about 55% in the corners of the screen, so the last rows of a long settings list and the right-hand switches become dim gold on near-black. The grain (alpha 0.45) also sits on the text.
Fix: in settings use the grain only, or a much weaker vignette (rim alpha ~0.3), and draw it BEHIND the list (put `GrainAndVignette` before `LazyColumn` in the Box) so text stays on top. It is static (no battery cost).

## 5. MEDIUM (look): LauncherCard frame drawn even when the card is meant to be invisible
`component/LauncherCard.kt:30-36`. The panel gradient follows `backgroundOpacity`, but `medievalFrame()` is always drawn. The search bar (`component/SearchBar.kt:139`) with the "transparent" or "hidden" style and widgets set to a transparent background will now show a bare gold double frame. Also `shadowElevation` is still set when opacity is 1 while the Surface colour is `Transparent`: Android draws the shadow of a see-through outline and it can show as a dark smear on the panel (PLAUSIBLE, check on a screenshot).
Fix: draw the frame only when `backgroundOpacity > 0f`; give the Surface `shadowElevation = 0.dp` (the panel is flat in C5) or pass `color = Medieval.PanelBottom`.

## 6. LOW (clipping): the first result group's ribbon can be cut by the list's top edge
`launcher/search/common/list/ListResults.kt:189`. The ribbon sits 8 dp above its item (`offset(y = -8)`) and for the first item the top padding is 0, so when that item is the top of the results list (or scrolled to the top edge) the LazyColumn clips the upper half of the ribbon.
Fix: when `showRibbon && !reverse` add 8 dp top padding to that item (inside the frame's `topInset`), or set the list's `contentPadding.top` to at least 8 dp.

## 7. LOW (scope / settings): theme pickers silently stop working
`theme/LauncherTheme.kt:67-75`. Colours, shapes and fonts are fixed and `LocalDarkTheme` is always true, so Settings > Appearance colour scheme, shapes, typography and the light/dark choice change nothing (S2's handoff says so). Not a bug, but the owner will see menus that do nothing.
Fix (owner choice): hide those entries, or add a one-line note in them ("Deliverance uses its own look").

## 8. LOW: `PennantHandle` replaces Material's drag handle without its accessibility actions
`component/DismissableBottomSheet.kt:271`. `BottomSheetDefaults.DragHandle` carries "expand/collapse" semantics for TalkBack; the pennant is a plain drawing. Only matters with TalkBack.
Fix: add `Modifier.semantics { contentDescription = "Drag handle" }` (and the expand/collapse custom actions if wanted).

## Checked, no problem found
- Upstream files touched match the S2 brief (theme, shared components, import swaps, result lists); no new `catch`; nothing animates in a loop (switch slides once, 120 ms); new code sits in `medieval/`.
- `medievalFrame` / `Ribbon` order: ribbon drawn after the frame everywhere, so no gold line crosses it.
- ListResults frame logic (`topLine`/`bottomLine` with `isBeforeExpanded`/`isAfterExpanded`): correct for both list directions.
- Colour contrast of the scheme itself: `onSurface` E6D9B8 on `surface` 12100C and `onPrimary` Ink on Gold both read well.
