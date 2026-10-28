# Brief: R1-review (coordinator, 2026-10-27), model: Opus
Branch claude/R1-landscape-fix (worktree r1-landscape-fix-06037a, cut from claude/S1-landscape: pushed, build green, owner-tested BROKEN in landscape).
Why: two or more fixes cost the owner too many rebuilds; a stronger model double-checks the code and makes the UI visible on this PC.

Owner's phone result for S1 (S20 FE, 1080x2400, nav buttons on the right in landscape, camera hole on the left; Auto-rotate on):
1. whole stage drawn in the top half; header and Ledger heading cut off at the top; bottom half empty;
2. the four wax seals (dock) overlap the lower shields (YouTube, Messages) and their labels;
3. a horizontal line cuts through the orb ring;
4. the command bar overlaps the Ledger rows and Dispatches ("No dispatches" under the bar, Dispatches heading gone);
5. the weather picture crowds the divider.
Pill on the left edge looks right. Portrait is fine and must stay unchanged.

Tasks, in order:
A. Make the app visible here: probe the Android emulator (acceleration, AVD shaped like the S20 FE, local debug APK, screenshots portrait + both landscape turns, emulated hole cutout). If it cannot run, say exactly why and set up the next-best way to see rendered Compose screens (screenshot tests, test-only deps). Write `ops/emulator.md` with exact commands; propose a Definition-of-done line (screenshots of changed screens reviewed before READY TO PUSH).
B. Root-cause bugs 1-5 in `MedievalHomeComponent.kt`, `MedievalLandscape.kt` and the sections, confirm with screenshots, write causes in `tasks/R1-review/bugs.md`, fix so landscape matches `docs/design/S1-landscape.png` (layout A, three columns, orb centred, dock row under the orb, no overlaps, stage centred and scaled to the free area both ways, nothing cut, nothing under the camera or nav bar). Verify portrait (unchanged), landscape left/right, small screen if cheap, typing layer open in landscape. Small fixes inside `medieval/`.
C. Review S2 branch `claude/s2-screens-1156ca` read-only; findings ranked with file:line and fix in `tasks/R1-review/review-s2.md`. Do not edit S2.
Done: ops/emulator.md, bugs.md, review-s2.md, fix committed, screenshots in `docs/design/shots/`, testlist, handoff, lessons, STATUS row; READY TO PUSH with what is NOT verified. Never push or build without the owner's yes.
