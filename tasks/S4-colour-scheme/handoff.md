# Handoff: S4-colour-scheme (Part 2 built 2026-10-28)
Picks (owner): 1a five palettes incl. Royal Blue and Silver; 2a default Gold and Oxblood; 3b weather takes the palette; 4b separate orb pictures; 5a matching asset sets later (S3); 6a small text lifted; 7 monetisation track in ops/monetization.md.
What changed:
- NEW `medieval/MedievalPalette.kt`: `Palette` class, five palettes, `readPalette`/`writePalette` (SharedPreferences `medieval_home`, key `palette`).
- `MedievalTheme.kt`: `Medieval.X` colours are now getters on `Medieval.palette` (Compose state), so the 110 existing uses needed no edit and recolour live. New roles added (Label, RibbonText, GoldLight, Glow, Heart ...).
- About 55 hard-coded colours in 17 medieval files now use palette roles (list: `git diff` of the S4 commit). Fixed on purpose: pure black camera pill/progress backs, the near-black pennant back, rain blue in weather mixes with gold, white sparkles.
- Settings: `MedievalPalettePreference` in `MedievalSettings.kt`; one line in `settings/main/MainSettingsScreen.kt` (upstream file, own commit).
- Orb: 12 new webp in `drawable-nodpi` (4 palettes x smoke/glass/setting, +about 410 KB: 4 x 77 KB less compression gains: files total about 440 KB). Made by `docs/design/S4-orbrecolour.html` (colour-graded from the originals, gem recolour for Verdigris/Royal), check picture `docs/design/S4-orbs.png`.
- Default palette (Gold and Oxblood) keeps original values except: faint grey 8C7A52 -> 9A8760 and soft red C0534A -> D0645A (pick 6a); ring inner line and a few mixes are within a shade of the old ones.
Not done / UNVERIFIED: runs on the phone (never tested); the cap letter gradient reads `Medieval.palette` each draw (allocation per frame is small; check no jank); S2 screens (`DeliveranceTheme.kt`, branch claude/s2-screens-1156ca) still build a fixed Material colour scheme: after S2 is merged make that scheme a function of `Medieval.palette`. Matching wallpaper/lock/keyboard sets per palette: S3.
Compile: `./gradlew :app:ui:compileDebugKotlin --offline` green.
