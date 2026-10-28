# Handoff: S1-landscape stage 1 (Part 2 built)

Branch `claude/S1-landscape` (design preview + code; nothing pushed, no build started). Owner picks: layout A, standing pill P1, dock as a row.

What changed
- Upstream manifest (own commit): `LauncherActivity` has `configChanges="orientation|screenSize|smallestScreenSize|screenLayout"`, so a turn no longer rebuilds the activity (typed text, open typing layer and other state stay). `uiMode` is not listed, so a dark/light switch still rebuilds.
- New `MedievalLandscape.kt`: `LocalLandscape`, `LocalColumnWidth`, stage rectangles per section for portrait (390 x 842) and landscape (760 x 374), `stageGeo` (scale and position inside the free area), `RotateGlide` (350 ms, one shot; sections slide, scale and fade in from where they were in the old layout; snaps when `Motion.allowed` is false; not played on the first composition), `RotationAnimationEffect` (asks for `ROTATION_ANIMATION_CROSSFADE`).
- `MedievalHomeComponent.kt`: reads the real left/right navigation-bar and camera-cutout insets in landscape (6 dp margin on the camera side) and builds both layouts. Portrait keeps its numbers (each section now sits in its own scaled body box inside a glide wrapper).
- Section changes (portrait unchanged): `Panel` takes its width from the column; Treasury (landscape layout), Ledger and Dispatches (12 dp inset, row width from the column), Command bar (smaller text), Pennants (row laid out 247 wide, drawn at 0.9), Dock (divider and row width from the column), Header (compact left-column version: day line, weekday, date, small medallion, clock left, weather and scene right). The orb zone is drawn at 0.9 so its shields fit the 296 wide middle column.
- `MedievalPill.kt`: `findSideHole` (cutout on a left or right edge, the AF-3 box-from-edge reading turned sideways) and `StandingPill` (notification icons above the hole; Hushed, Bluetooth, Wi-Fi, signal below). Used when the window is landscape.
- Typing layer: horizontal cutout/nav-bar insets on the overlay; in landscape `CommandLayerContent` shows the apps and the contacts/switches panes side by side.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL after each step.

NOT verified (the workspace cannot run the app)
- Everything on the phone. The look was built from numbers and the preview `docs/design/S1-landscape.png`, never rendered from the app. Header is the riskiest part: a long weekday ("Wednesday") may touch the small medallion; the weather scene under the weather line may crowd the divider.
- Whether the navigation bar and the camera cutout insets are the sides expected (read from real insets, so either side works), and where the hole really sits in the cutout box (log line `Pill: landscape cutout ...` in tag `medieval` prints rect, outline and result).
- That One UI 5 honours the cross-fade rotation animation; if not, Samsung's own turning animation plays under our slide.
- That `configChanges` has no side effects on other launcher screens (search page, widgets page, settings sheets) when turned; upstream recreated on rotation before.
- Taps during the glide; the glide with the typing layer open; the shield picker and header long-press in landscape.
- Long text in the 198 wide Dispatches and Ledger rows (cut with "...").
- Rotation lock: the upstream setting "Fixed screen rotation" (off by default) forces portrait when on, and the phone's own Auto-rotate must be on.

Next stage needs: owner feedback on how each section looks in landscape (sizes, header collisions). Fix as a NEW thread from brief + this handoff + bugs.
