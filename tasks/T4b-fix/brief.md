# Task brief: T4b-fix

Goal: Fix the two bugs in `tasks/T4b-fix/bugs.md` found on the T4-header build. Branch `claude/T4b-fix` from `claude/T4-header` (already pushed, build green). Read `tasks/T4-header/handoff.md` first.

Part A, layout: make every section centred and evenly spread on a real phone, and keep the dock clear of the Android navigation bar (respect window insets for bottom and top; the content must fit the free height, scale down rather than clip or overlap). Check all horizontal positions against the panels (pennants span the same width as the panels; four seals evenly spread; shields symmetric). Keep the approved look; the 8% growth stays unless the free height forces a smaller factor (then use the largest factor that fits and say what it is). Test arithmetic for 1080x2400 (S20 FE, 411x~914 dp with status bar and 3-button nav bar) and also a gesture-nav phone and a smaller 360x640 dp screen.

Part B, weather: find out why no weather shows. Verify with `api-scout` how Kvaesitso picks a weather provider and what the owner must tap (settings path, location permission). Then:
- Give a plain-words step list in the testlist for the owner to set up weather in the launcher settings.
- Make the launcher settings reachable from the Home screen if they are not already (check what long-press on empty space does). If a new entry point is needed, that is a design choice: ask the owner multiple choice first (for example A) long-press the header, B) long-press empty space, C) a small gear on the weather spot).
- When no provider is set, the weather spot should say "Tap to set up weather" (placeholder style) and open the weather settings, only if the owner agrees in the same question.

Rules: commit locally; NEVER push or start a build without the owner's approval; do the whole batch, compile with `./gradlew :app:ui:compileDebugKotlin` (JAVA_HOME = Android Studio jbr, as in the T4 handoff), then report READY TO PUSH with a plain-words summary. Commit trailer: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.
Done when: gate items 2-4, testlist (visible result first; should-NOT-happen step: dock labels hidden behind the nav buttons; restart step; weather setup steps in plain words), handoff, lessons (0-3), STATUS row. Two failed fixes of one bug: stop and write the root cause in bugs.md.
Out of scope: new designs, camera pill, Treasury.
