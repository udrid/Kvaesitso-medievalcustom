# Bug: the camera pill sits too high (third report; escalated to Opus per CLAUDE.md)

History
1. T5-pill (first build, 2026-10-26): owner photos `docs/design/photos/T5-pill-phone-1.webp`, `T5-pill-phone-2.webp` (taken sideways because screenshots do not show the camera hole): hole sits about 5-6 dp LOW inside the pill (pill too high).
2. T5b-fix fix 1: centre on `displayCutout.cutoutPath` bounds when available (API 31+), else the rect centre moved 5 dp down. Pushed, built green (run 40420926486).
3. Owner test of that build: "the pill is still too high" (no photo yet). We do NOT know whether the path or the 5 dp fallback was used, how far off it is, or by how much.
Two Sonnet fixes failed: stop guessing.

Hypotheses to test with research, not with guesses:
- The coordinate space is wrong: the Compose root view may not start at screen y=0 (insets/padding, edge-to-edge or not, `layoutInDisplayCutoutMode`), so cutout coordinates (window or screen space) are applied in a different space (use `view.getLocationOnScreen`, `getLocationInWindow`, `WindowInsets` and the display's real size).
- The status bar being hidden changes the window's top inset after the hole is read (read order / stale value).
- Samsung reports a cutout rect and path that do not match the visible camera glass; other apps (Dynamic Island style overlays, Samsung's own camera-cutout UIs, notch-aware apps) place content using other APIs (`DisplayCutout.getBoundingRectTop`, `getSafeInsetTop`, `Display.getCutout()`, `getWaterfallInsets`, or a fixed per-device table).
- Pill internal layout: the hole is centred on the pill's centre, but the pill's own height or padding is asymmetric (hole circle drawn off-centre inside the pill).

## Root cause (T5c, Opus; strong inference, NOT yet proven on the phone; see research.md)
Checked and ruled out:
- Coordinate space: theme `shortEdges` + `enableEdgeToEdge` + `setDecorFitsSystemWindows(false)` mean the window starts at screen y=0 and does not move when the bar hides; cutout rects and `positionInWindow` are both window pixels. Consistent.
- Pill inside: background at y=0 with the full height, hole box placed at (height - hole)/2. Symmetric; not the cause.
Likely cause: phone makers describe a punch hole as a box from the top edge of the screen (y=0) down past the hole (AOSP path string, example from a Xiaomi overlay). `boundingRects` and `cutoutPath` are then the same box, so T5b-fix's switch to the path changed nothing. The pill was centred on the middle of that box, which lies above the hole's centre. The drawn hole size (min of width/height = box width) still matched, which fits this.
Fix: if the outline starts at the top edge and is taller than wide, the hole is a circle as wide as the box at its bottom (centre = bottom - width/2). 5 dp guess removed. Residual risk: if Samsung adds a margin below the hole, the pill is still a little high; the on-screen numbers (diagnostic) will show the box and let us correct exactly.
