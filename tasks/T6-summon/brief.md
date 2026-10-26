# Task brief: T6-summon

Goal: The command bar gets the SUMMON button from C5 (`docs/design/C5.html`). Tapping it hands the typed text to the Claude app as a question. Branch `claude/T6-summon` from `claude/T5b-fix`. Read the T1-home and T4b-fix handoffs first.

Owner decisions (`ops/decisions.md`): the command bar stays the launcher's search (apps, contacts, calendar, settings, web); SUMMON hands the typed text to the Claude app as a question; the orb opens the Claude app empty; no Claude API key. Marked UNVERIFIED there: whether the Claude app accepts text from another app.

Part 1 (research, no code; report and stop if the answer is "no"):
Use `api-scout` and the web to find out how another app can send text to the Claude Android app (package `com.anthropic.claude`): a share intent (ACTION_SEND text/plain), ACTION_PROCESS_TEXT, a deep link or URL scheme (for example claude.ai/new?q=...), or an assistant integration. Find which one opens a NEW chat with the text prefilled, and whether it sends automatically. Check the manifest of the real app if you can (public sources only). Report the ranked options with certainty levels (VERIFIED / likely / UNVERIFIED) and propose a tiny probe the owner can test on the phone. If nothing works, say so and give the owner the fallback options as multiple choice (for example: copy the text to the clipboard and open the Claude app; or open claude.ai in the browser with the question in the link). Do not build until the owner picks.
Part 2 (after the owner picks an option): build SUMMON in new files under `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/`, keep search behaviour unchanged, empty text = SUMMON does nothing visible except a short hint. No new permissions. Compile with `./gradlew :app:ui:compileDebugKotlin --offline` (JAVA_HOME = Android Studio jbr).

Rules: commit locally; NEVER push or start a build without the owner's approval (CLAUDE.md). Commit trailer `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Done when: gate items 2-4, testlist (visible result first, should-NOT-happen step, plain steps), handoff, lessons (0-3), STATUS row, then report READY TO PUSH. Add the verified fact to lessons as a proposed `api-facts.md` line (only the coordinator edits that file).
Out of scope: Treasury, shield artwork, any other redesign.
