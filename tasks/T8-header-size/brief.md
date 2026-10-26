# Task brief: T8-header-size

Goal (owner, 2026-10-26): on the Home header, make the clock, the weather line and the weather picture (scene) slightly larger, and stop the weather picture from touching or crossing the gold divider line that runs under the header. Branch `claude/T8-header-size` from `claude/T5c-pill-position`.
Read `tasks/T4-header/handoff.md`, `tasks/T4b-fix/handoff.md` and `docs/design/T4-header.html` first.

Do:
1. Make the clock, the weather text and the weather scene about 15% larger ("slightly"; keep the same right-hand column alignment and style). Check the weather text still fits (two lines max at the new size; ellipsis only as a last resort).
2. The weather scene currently overlaps the divider line under the header. Fix the geometry so the scene sits fully above the divider with a small gap (move up or shrink the scene's vertical extent, or shorten the divider where it meets the scene; pick the option that keeps the C5 look and tell the coordinator which). Nothing else in the header, the pill, or the sections below may move; if the larger clock needs more height, find the room inside the header block and report it.
3. Check all seven weekday headers (Wednesday is the longest name) and a 360 dp wide screen for collisions between the day name, medallion, clock, weather text and scene.
Rules: commit locally; NEVER push or start a build without the owner's approval (CLAUDE.md); compile with `./gradlew :app:ui:compileDebugKotlin --offline` (JAVA_HOME = Android Studio jbr). Commit trailer `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Done when: gate items 2-4, `testlist.md` (visible result first; should-NOT-happen: scene touching the line, anything cut off or moved; restart step), `handoff.md`, lessons (0-3), STATUS row, then report READY TO PUSH with a plain-words summary.
Out of scope: any other design change, the pill, gestures, Treasury.
