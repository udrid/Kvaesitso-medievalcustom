# F1 final check: `claude/integrate-4` (R1 + S2 + S4 + S3), checked on `claude/f1-final-check-35a943`

## VERDICT: PASS (after the F1 fix commits on this branch), plus 3 ASK OWNER questions that do not block the push
Push THIS branch (it contains integrate-4 plus 4 F1 commits), not integrate-4 alone. Confidence: high for layout and colours (rendered); phone-only behaviour unverified (below).

## Fixes applied here (separate commits)
1. `93a1602e6` (upstream file) Settings > Appearance: light/dark, colours, typography and shapes pickers hidden (owner pick). Transparency, import/export and colour source remain.
2. `420bedb77` Merge slips, compile-correct but wrong:
   - BLOCKER-class: `Deliverance.colorScheme` (S2) was a fixed `val` built once, so settings, search results, dialogs and sheets stayed Gold and Oxblood whatever the palette (S4 handoff asked for this after the merge; nobody did it). Now a getter on `Medieval.palette` (cached per palette); hard-coded surface tones mixed from palette roles (same values in Gold and Oxblood).
   - Landscape Treasury bar (R1/S1 code) still had the pre-S4 hard-coded colours: now `palette.barA/barB`.
   - Ribbon text, wax-seal switch (on highlight, off seal, track) hard-coded: now palette roles.
   - Pennant back `Color(0xFF1A1610)` read as maroon/brown in Verdigris and Royal Blue (seen on screen): now mixed from the palette's ink/panel (same value in Gold and Oxblood).
3. `f824bc703` (upstream file) `LauncherTheme` loads the saved palette once per process, so Settings reopened after the app was killed show the chosen scheme, not the default.
4. `1052cfc11` Test `F1Shots.kt` + 31 pictures in `docs/design/shots/F1/`.

## Checks
- MERGE: re-ran the three merges with `git merge-tree`; only Pennants.kt (code) and docs conflicted. Pennants resolution = R1 layout + S4 palette colours, correct. Docs resolutions keep both sides. Gold portrait and both landscape turns are pixel-identical to R1's pictures except the clock. No other hard-coded colour left in R1/S1/S2 code except S4's deliberate ones (pill blacks, sparkles, rain).
- R1's S2 review: findings 1-7 and 11 fixed in code and on screen; 8 accepted; 9 (slider) made square; 10 done by fix 1.
- S3: `build-iconpack.yml` is manual only (`workflow_dispatch`), no secrets; `iconpack/` is a standalone build (`-p iconpack`), not in the root `settings.gradle.kts`; launcher workflow still runs `assembleDefaultNightly` only. `assets/` (6.8 MB) is outside the app module. Launcher APK grows by about 227 KB (12 orb webp). `iconpack/LICENSE-ART.md` present (placeholder, all rights reserved); monetisation rows present (nit: `ops/monetization.md` has duplicate "planned" and "S3 preview" rows for icon pack/wallpaper/SwiftKey after the merge).
- MOTION/BATTERY: rotate glide one-shot 350 ms, skipped when `Motion.allowed` is false and on first composition; wax seal slides once (120 ms); nothing new loops or polls; palette reads add no timers.
- CODE: no new `catch`, no personal data, logs carry geometry only. Manifest: only S1's `configChanges` (own commit).
- BUILD: `:app:ui:testDebugUnitTest` (compiles app:ui) green offline with all three screenshot tests. Lint not run.
- SEE IT (looked at every picture): Home portrait in all 5 palettes, both landscape turns (Gold, Ember, Bone, Royal), typing layer landscape, 360x640 portrait, 640x360 landscape, font 130 % portrait and landscape; S2 settings/results/dialog/sheet+cards in Gold, Ember, Bone, Royal, Verdigris, landscape and font 130 %. No overlaps, nothing under the camera or nav bar, no gold line through a ribbon, sharp corners on panels/cards/dialogs/sheets.
- Seen, pre-existing (not from this merge): Ledger 2nd row half under the panel's bottom line in portrait (same in owner-verified main); landscape command bar placeholder ends "Command the Oracl..."; Home ignores font size (fixed stage, by design).

## ASK OWNER
1. Buttons in dialogs ("Cancel", "Hide") are still round pills, and the typing-layer switch chips (Wi-Fi, Bluetooth...) are round, while panels are sharp. A) make buttons and chips square like the panels (recommended, small task); B) keep them round.
2. Appearance still shows Theme import/export and "Colour source", which no longer change anything. A) hide them too (recommended); B) keep them.
3. Wax-seal switches are round (a seal is round). A) keep (recommended); B) square.

## NOT verified (needs the phone)
Real camera pill position in landscape (reads the real cutout), Samsung's rotation animation vs ours, `configChanges` effect on search/widget pages, taps during the glide, real apps/contacts/widgets in the restyled screens, bottom sheets inside the real overlay, live palette switch from the real Settings activity, palette after the process is killed (fix 3, code only), icon pack / wallpaper / AOD / keyboard (S3 testlist), smoothness and battery.
