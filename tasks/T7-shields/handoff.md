# Handoff: T7-shields stage 1

What changed: every Home shield now shows a gold line emblem or, for apps without one, a gold letter shield; the app's coloured icon is never used. New file `ShieldEmblems.kt` (15 emblems as path data, package-to-emblem lookup, `EmblemIcon`, `LetterShield`, `ShieldEmblem`). `Shields.kt` (upstream-style small edit): removed the `glyph` field and the coloured-icon bitmap; the shield now calls `ShieldEmblem(pkg, label)`. Badge, picker, saved picks and the dock are untouched. Design preview: `docs/design/T7-shields.png` (owner liked it, 2026-10-26).
How it was checked: `./gradlew :app:ui:compileDebugKotlin --offline` succeeds.
NOT verified: how it looks on the phone (thin strokes of lute, trumpet, quill at 26 px); that the package names in the lookup match the S20 FE (written from memory: dialer, camera, gallery, clock, calendar, Messages); an unlisted package simply shows the letter shield.
Next stage needs: owner phone test (`testlist.md`); to add an emblem for a missed package, add its name to `byPackage` in `ShieldEmblems.kt`.
