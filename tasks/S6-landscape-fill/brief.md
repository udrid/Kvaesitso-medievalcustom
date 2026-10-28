# Brief: S6-landscape-fill (stage 1, preview only)

Branch `claude/s6-landscape-fill-a370fa`, cut from the Opus-checked combined branch (F1). Role: `phone-stylist`. Out of scope: portrait Home, colour scheme, gestures (S5).

Owner feedback (2026-10-28, phone test of landscape Home, screenshot sent): "lots of empty space. Come up with ways to fill it, maybe even adding more information or another panel as well as rearranging and increasing size."

Part 1 (this stage, no Kotlin):
1. Measure the real free space from `MedievalLandscape.kt` and the F1 landscape pictures: stage rectangle versus free area, both turns, share of screen empty and where.
2. Propose 3-4 ways, each previewed at 844 x 390 (note the 914 x 411 dp reality): A scale to the full free area; B taller columns (more rows, bigger pennants); C a fourth panel (candidates with data source, permission, battery, clutter); D A+B+one extra panel. Keep the orb the hero, camera pill and nav bar clearances, palette-aware C5 look, motion and battery rules. Portrait unchanged as baseline. Numbered list of changes and build cost.
3. Deliver `docs/design/S6-landscape-fill.html` + `.png`, mark [M] parts, update `ops/monetization.md` if needed. Report READY FOR DESIGN OK with numbered choices, then STOP.

Part 2 (build) only after the coordinator forwards the owner's picks. State what is NOT verified.
