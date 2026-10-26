# Handoff: T8-header-size stage 1

What changed (branch `claude/T8-header-size`, 3 small edits in `app/ui/.../ui/medieval/`, no upstream files):
- `HeaderSection.kt`: clock 25 -> 29, weather line 11 -> 12.5 (setup hint 10 -> 11.5), weather box 64 -> 74 wide (hint 76 -> 84). About +15%.
- `WeatherScene.kt`: scene 64 x 26 -> 74 x 30 (same shape).
- `MedievalParts.kt`: `MText` got an optional `lineHeight` (default 0 = unchanged for all other callers).
- Divider fix = option "move up / tighter stack", divider NOT shortened. Cause: the right column started 9 below the header top and the Compose line boxes were taller than the text, so it ended about y=90 on a divider line at y=87 (header top + 80 + 7). Now the column starts at the header top and uses fixed line heights: clock 0-31, weather 33-47, scene 50-80. Gap to the divider line about 7 stage units. The room came from removing the 9 offset, the 5 -> 2 gap above the weather, and the fixed line heights.
- Nothing else moved: day name, medallion, divider, pill, body below.

How it was checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL. By arithmetic: Wednesday text (drop cap + name) ends near x=175 against the medallion box at x=204, unchanged by this task.

NOT verified (needs the phone):
- The real gap between scene and divider (font line boxes are from my numbers, not seen).
- Clock "10:45"/"12:45" at 29 (about 75 wide, starts near x=271) vs the medallion ring (edge near x=278-288, widest on Mars/Jupiter days): a slight touch is possible.
- Weather text is still ONE line with ellipsis (74 wide); a long condition like "Thunderstorm with rain" will be cut. Two lines would not fit above the divider without moving things.
- 360 dp wide screen: the stage scales as one block, so proportions are the same as on the S20 FE; not seen.
- Clock top may sit a few px higher than before (the old 9 offset is gone, the line box is 31 tall).

Next stage needs: nothing.
