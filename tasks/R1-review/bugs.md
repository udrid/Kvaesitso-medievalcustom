# R1-review: S1 landscape bugs, causes and fixes

Seen on the owner's S20 FE (S1 build) and reproduced on this PC with the screenshot test (`ops/emulator.md`): the first render of the S1 code showed bugs 2, 3 and 4 exactly as on the phone.

## Root cause (bugs 1-4): `requiredSize` larger than the parent is CENTRED
`LandscapeSections.Place()` (MedievalHomeComponent.kt) reuses each section's portrait internals by giving it a box `requiredSize(r.w, r.h + bodyY)` offset up by `bodyY` (the section's portrait y). For the lower sections that box is taller than the 374-unit landscape stage: Dock 88 + 558 = 646, Dispatches 551, Command 448, Ledger 398. When a `requiredSize` exceeds the parent's max constraint, Compose reports the coerced size to the parent and places the child CENTRED in it, so each section moved up by half its overflow:
- Dock up 136 units -> its seals land on the lower shields and labels (bug 2) and its divider line runs through the orb ring (bug 3).
- Dispatches up 88, command bar up 37, Ledger up 12 -> the command bar sits on the Ledger rows, the Dispatches heading hides under the bar, "No dispatches" shows under it (bug 4); the Ledger ribbon goes above the screen's top edge (part of bug 1).
- With the dock and Dispatches pulled up, the bottom half of the screen is empty (bug 1).
- The same trap in `Pennants()` (landscape row laid out 247 x 126 inside a 222 x 113.6 box) moved the pennants 12.5 left and 6 up.
- The same trap at the stage level (`requiredSize(760, 374)` / `(390, 842)`) shifts the whole stage on any window narrower or lower than the stage (seen in the 640 x 360 dp render). It does not happen on the S20 FE (stage fits), so the phone's portrait was never affected.
Fix: `Modifier.overflowTopStart()` (= `wrapContentSize(Alignment.TopStart, unbounded = true)`) before each such `requiredSize`: the box keeps its size but starts at the parent's top-left corner. Applied in `Place()`, the Oracle box, the stage box and `Pennants()`.

## Bug 1, remaining part: the header cut at the top edge
On the phone the whole stage sits about 8-10 dp higher than in the PC render (measured from the screenshot: Treasury, pennants, orb and seal labels all ~9 dp higher), which cuts "DAY OF MERCURY". The PC render with the same insets does not show it, so the cause is phone-side and NOT confirmed: most likely the scaffold places Home's box above the window top in landscape (the camera pill already corrects for its own box position with `positionInWindow`, T5).
Fix (defensive): in landscape the free area is clamped to the part of Home's box that is inside the window (`positionInWindow` of the box, measured once per orientation so page swipes do not resize the stage). A log line `Stage: box ... at y ..., window h ... -> scale ... at x,y` (tag `medieval`) prints the real numbers on the phone. If the header is still cut, that line gives the cause.

## Bug 5: weather picture crowds the divider
Landscape header: clock/weather row at y 84, weather line ~19 tall, scene 30 tall with 3 padding -> scene ends ~136, the divider sits at 130-139.
Fix: row at y 80, scene drawn 56 x 23 with 1 padding -> ends ~123, clear of the divider.

## Found while checking (not in the owner's list)
- Ledger rows in landscape had no width limit, so a long title ("Daylight Saving Time ends") ran past the panel edge into the nav-bar side instead of ending in "...". Fix: in landscape the row is the column width minus 24 and the title takes the rest (ellipsis).
