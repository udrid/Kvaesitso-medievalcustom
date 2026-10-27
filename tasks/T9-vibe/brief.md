# Task brief: T9-vibe

Goal (owner, 2026-10-26): (1) the Latin ring text around the orb gets more style; (2) the Home screen becomes slightly reactive/animated, with battery life in mind: reactions and animations are mostly minimal, only add to the overall vibe, and never take over attention. Branch `claude/T9-vibe` from `claude/T8-header-size`.
Read `docs/design/C5.html`, `docs/design/T4-header.html`, `tasks/T4-header/handoff.md` (it already has a small stop-motion weather animation, about 12 redraws per second, whose battery cost is unknown) and `Orb.kt`.

BATTERY RULES (hard, apply to every animation):
- Nothing animates unless Home is visible and resumed (stop on ON_PAUSE/ON_STOP and when the screen is off).
- No animation while Battery Saver is on, or battery <= 15% and not charging; honour Android's "Remove animations" setting (animator duration scale = 0).
- Animate only small regions (separate draw layers); no full-screen redraw loops; cap at 12 fps for ambient effects, 60 fps only for a touch reaction lasting under 300 ms.
- Ambient effects must be slow and subtle (low contrast); at most TWO ambient effects run at once.
- Report an honest estimate of the cost; the owner can later check Android's battery usage for Deliverance. Add one setting in the launcher settings: "Animations: Full / Minimal / Off" (default Minimal), saved.

Part 1 (preview, no Kotlin; the owner has NOT chosen yet, so offer choices):
a) `docs/design/T9-ring.html` + `.png`: THREE ring-text styles for the same words (NOSCE TE IPSUM, SAPERE AUDE, LUX ET VERITAS; Cinzel Bold on the ring, as built): 1 illuminated lettering (gold with thin dark outline and soft glow, small red initial per phrase, tiny ornaments between phrases); 2 engraved metal (inner shadow plus light edge); 3 blackletter capitals (UnifrakturMaguntia, gold, flourishes). Same size ring as the real one (about 8% bigger orb on a 390 wide stage).
b) `docs/design/T9-vibe.html` (animated, CSS/JS, viewable in a browser) demonstrating a MENU of candidate animations at their real subtlety, each toggleable: orb flame flicker; a soft light drifting once around the ring about every 20 s; tap reactions (gold flash and tiny scale on shields, wax seals, the orb); one-time glow when a notification arrives (dispatch row and shield badge); slow breathing glow on the camera pill? (propose only if it is cheap). Next to each, write its battery cost rating (none / very low / low) and how it is kept cheap. Render stills to `T9-vibe.png`.
Commit locally and report READY FOR DESIGN OK to the coordinator with a plain-words description and STOP. The owner will pick the ring style and which animations; do not start Part 2 before the coordinator forwards that.
Part 2 (after the owner's picks): build only the picked ones in new files under `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/` with the battery rules above, the Animations setting, compile (`./gradlew :app:ui:compileDebugKotlin --offline`, JAVA_HOME = Android Studio jbr), testlist (visible result first; should-NOT-happen: anything moving that the setting turned off, anything distracting; a check with Battery Saver on; a restart step), handoff, lessons (0-3), STATUS row, then report READY TO PUSH.
Rules: commit locally; NEVER push or start a build without the owner's approval (CLAUDE.md). Commit trailer `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.
Out of scope: layout changes, the pill, gestures, Treasury.
