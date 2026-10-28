# Brief: T17-small (owner said "fix the small items", 2026-10-27)
Branch claude/T17-small (from claude/T16-polish). Three items, all already-approved ideas, no new design:
1. New-day shimmer of the red capital (T9 picks.md, extra E): one soft gold sweep (~1.5 s) when a new calendar day starts while Home is on screen, or Home resumes on a different date than last shown. Once per new day. Last date in SharedPreferences `medieval_home`. Obeys the motion gate (`MedievalMotion.kt`); gate closed = do nothing, still record the date. `rememberOneShot`, no ambient cost.
2. Weather line fits its box whole: shrink text down to ~10 pt (clock and scene unchanged), then short-form condition words; temperature always visible. Handoff states the longest result and width by arithmetic.
3. Battery leftovers: every ticker/poller in `medieval/` stops when Home is not resumed / screen off; what the owner sees is unchanged. List in handoff.
Done: compile green, testlist, handoff, lessons (0-3 lines), STATUS, READY TO PUSH. No push without owner yes.
