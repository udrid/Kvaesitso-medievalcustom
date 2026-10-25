# Handoff: T1b-polish stage 1

What changed (one commit each, branch `claude/T1b-polish`, all in `app/ui/.../ui/medieval/`, no upstream edits):
- StatusLine.kt, HeaderSection.kt, MedievalHomeComponent.kt: "Wi-Fi call" and "Voicemail" removed (Bluetooth, Hushed kept). The weather line moved out of the header column (it was overlapping the status row) into the right end of the status row, i.e. just under the clock. `HeaderSection()` no longer takes the forecast; `StatusLine(forecast)` does. `MedievalIcons.Wifi` and `Voicemail` are now unused (left in place).
- AppPicker.kt (new) + Shields.kt: the picker is a dialog (94% of screen width) with a search bar (filters by name, case-insensitive, contains) and a `LazyVerticalGrid`, 4 columns, icon above name (Cinzel caps 8.5, up to 2 lines), alphabetical, all launchable apps. 4 columns chosen: 5 would be about 67 dp wide on a 390 dp screen, too tight for names. Same shield save logic as before.
- TreasuryPanel.kt: `[YOUR TOTAL]` 21 -> 17 and limited to 160 wide (ends before the Quest block at x=234). Nothing else changed.

How it was checked:
- GitHub Actions build for `claude/T1b-polish`: run 40240997944 (last commit "3") green. No local compile.
- No new `catch`; the one added logs with tag `medieval`.

NOT verified (workspace cannot run the app):
- That nothing overlaps on the S20 FE; the weather text width next to Hushed with a long condition name (it can ellipsize).
- The picker look, icon loading speed with many apps, keyboard behaviour with the search field, names fitting 4 columns.
- A real total longer than 160 wide will be cut with "..." until T2 decides the final layout.

Clutter ideas beyond the brief (not done): none found beyond the owner's list.

Next stage needs: T2 Treasury can edit `TreasuryPanel.kt` freely now.
