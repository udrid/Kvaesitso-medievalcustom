# Task brief: T7-shields

Goal: every shield on the Home screen matches C5, with its own custom emblem instead of the app's own coloured icon or a plain glyph. Today (`app/ui/src/main/java/de/mm20/launcher2/ui/medieval/Shields.kt`) the four heater shields show X, Discord, YouTube, Messages with simple glyphs, and any other app the owner picks (e.g. Maps) shows its real coloured icon, which clashes. Branch `claude/T7-shields` from `claude/T6-summon`, own worktree.

Part 1 (preview, no Kotlin): `docs/design/T7-shields.html` + `.png` (390 wide, palette and fonts of `C5.html` / `T4-header.html`, inline vector drawn by Claude) showing: (a) emblems for X, Discord, YouTube, Samsung Messages, Maps, Phone, Camera, Chrome/Brave browser, Gallery, Settings, Clock, Calendar, Spotify, WhatsApp, Gmail (gold line-art on the charcoal field, small heraldic/medieval flavour per app); (b) the rule for any app without an emblem: gold shield with the app's first letter in blackletter, never the coloured app icon; (c) the shield with a badge. Commit, report READY FOR DESIGN OK to the coordinator, STOP. Do not start Part 2 until the coordinator forwards the owner's approval or changes.

Part 2 (after approval): new files under `app/ui/.../ui/medieval/`: emblem paths as Compose vectors, a package-name to emblem lookup, the generic letter fallback; badge keeps working; picker and saved picks unchanged. Compile (`./gradlew :app:ui:compileDebugKotlin --offline`, JAVA_HOME = Android Studio jbr). Then testlist (visible result first; should-NOT-happen: coloured app icon on a shield; restart step), handoff, lessons (0-3 lines), STATUS row, report READY TO PUSH with a plain-words summary.

Rules: commit locally; NEVER push or start a build without owner approval. Ask the owner multiple choice for any other design decision.
Out of scope: picker look, header, pill, Treasury, any other layout.
