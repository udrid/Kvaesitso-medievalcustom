# S5-gestures-apps brief (owner, 2026-10-28, on the phone)

Thread: S5-gestures-apps, acting as the `phone-stylist` agent. Branch claude/s5-gestures-apps-47305e (cut from claude/integrate-4 = f1-final-check).

## Owner request
"I want to make the gestures different and the menus they open different. Swipe down anywhere on the phone pulls up a BLANK search screen: DISABLE that completely. Work on the swipe-UP widgets page instead: I should have easy access to ALL my apps, sorted, FIRST THING below the search bar: maybe a bunch of CATEGORIES that expand when I tap them, and if needed PAGES. I want other functionality below this part too, so come up with ideas."
Earlier decision (2026-10-26): phone-wide gesture changes were deferred until Home was complete; now allowed.

## Part 1 (preview only, no Kotlin)
1. SURVEY the gesture code, the swipe-up page, the all-apps list; how to disable swipe-down search fully (also Home button, assistant); can the T11 forced-off all-apps list return inside the new page only.
2. CATEGORY sources (Android category, Kvaesitso tags, shipped curated list, owner drag), recommend one; expand/collapse, pages, counts, filter, long-press menu.
3. MENU OF IDEAS for what goes below the apps (source, permission, battery, clutter, [M]).
4. GESTURE TABLE (swipe up/down/left/right, double tap, long press, Home button).
Deliver `docs/design/S5-gestures-apps.html` + `.png` (390 wide frames, 2-3 variants, expanded and collapsed states, gesture table, numbered choices with cost and risk). Mark [M], update `ops/monetization.md`. Commit locally, report READY FOR DESIGN OK, STOP.

## Part 2
Only after the coordinator forwards the owner's picks.

## Out of scope
Home layout, colour scheme, assets. Never push; never start a build.
