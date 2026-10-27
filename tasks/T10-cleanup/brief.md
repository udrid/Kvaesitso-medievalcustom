# Task brief: T10-cleanup

Goal (owner, 2026-10-26): after the T8 phone test (pill centred, header good) two small fixes. Branch `claude/T10-cleanup` from `claude/T8-header-size`.
1. Revert commit 2e718ce8e (temporary on-screen pill numbers). Keep the centring fix and the log line (tag medieval).
2. Planet medallion vs right column (clock, weather, scene): on all seven days (Sol, Luna, Mars, Mercury, Jupiter, Venus, Saturn) keep a clear gap of at least 8 stage units. Measure from `PlanetaryDays.kt` and `HeaderSection.kt` (centre x, largest ring/ray radius, column left edge, 29 pt clock width for "10:45"/"12:45"). Fix by shifting the medallion left and/or shrinking the widest ornaments. Do not move the day name or anything else; if Wednesday then gets too close to the medallion, report it as an owner decision.
Rules: commit locally; NEVER push or start a build without owner approval. Compile `./gradlew :app:ui:compileDebugKotlin --offline`.
Done: gate, testlist.md (visible result first; date-change steps for all seven days, put date back to automatic; should-NOT: numbers under the pill, anything touching), handoff.md (final gaps for all seven days), lessons (0-3), STATUS row, READY TO PUSH.
