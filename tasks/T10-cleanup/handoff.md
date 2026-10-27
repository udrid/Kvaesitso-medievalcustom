# Handoff: T10-cleanup stage 1

What changed (branch `claude/T10-cleanup` from `claude/T8-header-size`):
- `git revert` of 2e718ce8e: `PillDiagnostic.kt` and its calls in `MedievalPill.kt` are gone. The centring fix (7cbf13fa2) and the `Log.i("medieval", "Pill: cutout rect ...")` line stay.
- `HeaderSection.kt`: medallion box x 204 -> 185 dp (centre x 248 -> 229 inside the 346-wide header box; 19 left). Nothing else moved (day name, clock, weather, scene, divider).
- `PlanetaryDays.kt`: widest ornaments pulled in so every day reaches at most ~34 units from the centre (was 40 on Mars, Mercury, Saturn, Luna 39.8, Venus 36, Sol 35): Sol ray tip 35->34; Luna stars/crescents moved in 1.5-6; Mars tips 40->33.2, 36->33, ticks 35->33; Mercury feather tips 40->33; Saturn octagon 34->31, diamond tips 40->33; Venus petal tips 36->32. Jupiter unchanged (32.2). Disc, rings and signs unchanged.

Measured (stage units = dp; header box x 0..346; fonts measured with the real font files):
- Right column left edge = 272 (weather box and scene are 74 wide; clock "10:45" 57.4, "12:45" 56.8 wide so the column is set by the 74 box). Real text starts later: clock at ~289, weather text "62° · Cloudy" (61.7 wide) at ~284.
- Before: centre 248, edge 288 on Mars/Mercury/Saturn = -16 against the column box (+0.6 to the clock text on Luna day, matching what the owner saw).
- After, medallion right edge (centre 229 + half extent incl. stroke/miter) and gap to the column edge 272:
  | Day | Right edge | Gap |
  |---|---|---|
  | Sol | 263.0 | 9.0 |
  | Luna | 262.5 | 9.5 |
  | Mars | ~263.1 | ~8.9 |
  | Mercury | ~262.6 | ~9.4 |
  | Jupiter | ~261.2 | ~10.8 |
  | Venus | ~262.6 | ~9.4 |
  | Saturn | ~262.7 | ~9.3 |
  Gap to the clock text itself is about 25 or more on every day.
- Left side: Wednesday (Mercury) day-name end measured 180.3 (drop cap 73.3 + 6 + name 101.0); Mercury's left feather edge is ~195.4 -> gap ~15 (before: 27.7). Other days: Monday ends 147.9, Thursday 147.1, Saturday 145.2, Tuesday 134.2, Sunday 126.9, Friday 115.9, all at least 47 clear. So no owner decision is needed for Wednesday.

NOT verified (needs the phone):
- Nothing drawn or seen; only a local compile (`:app:ui:compileDebugKotlin --offline`, BUILD SUCCESSFUL). Extents are arithmetic from the path data (miter joins estimated with the default miter limit 4); a half-unit error is possible.
- The gap is measured to the column BOX (conservative). While weather is not set up ("Tap to set up weather" etc.) the text box is 84 wide (edge 262, gap about -1 against the box); the text is right-aligned and wraps in short lines, so it should still clear, but not checked.
- Whether the smaller ornaments still look good (Mars, Mercury, Saturn lost about 15% of their reach).
- The pill itself was not touched beyond removing the numbers.

Next stage needs: nothing.
