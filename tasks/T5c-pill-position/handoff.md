# Handoff: T5c-pill-position

Cause (see `bugs.md` "Root cause", `research.md`): the cutout outline is very likely a box from the screen's top edge down past the hole (rect and path identical), so its centre is above the hole. Coordinate space and the pill's inner layout were checked and are correct.

Commits on `claude/T5c-pill-position` (on top of T6-summon):
1. `7cbf13fa2` fix, `MedievalPill.kt` `findHole` only: outline = path bounds (API 31+) or rect; if it starts at the top edge (top <= 2 dp) and height > 1.15 x width, the hole is a circle of the box width at the box bottom; otherwise the outline centre. The 5 dp guess is gone. Log line (tag `medieval`) prints rect, outline, method.
2. `2e718ce8e` TEMPORARY diagnostic: new `PillDiagnostic.kt` + a few lines in `MedievalPill.kt`. Three lines of 8 sp text under the pill:
   - `<path|rect>-<bottom|centre> hole y <px> pill y <px> d <hole px>` (pill y = measured centre of the drawn pill, window px)
   - `box l,t,r,b view <view top on screen px> bar <status bar inset px> dpi <density>`
   - `sys <Samsung's config_mainBuiltInDisplayCutout, first 60 chars, or ->`
   Remove with `git revert 2e718ce8e` once the position is confirmed.

How to read the owner's screenshot: if the photo shows the hole still high/low by N dp, compare `box` bottom with hole y; `sys` shows Samsung's real path (circle or box). If it is a circle, centre on it; if a box with a margin, the margin = box bottom - real hole bottom.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL after each commit. Nothing pushed, no build.
NOT verified: everything on the phone. That Samsung's outline really is a top-edge box (inference, no Samsung source found). Whether Samsung adds a margin below the hole (would leave the pill slightly high). Whether `config_mainBuiltInDisplayCutout` is readable by an app on One UI 5 (shows `-` if not).
Open owner question (not asked, not needed unless this build is still off): an adjustable "Pill height up/down in 1 dp" setting in the launcher settings.
