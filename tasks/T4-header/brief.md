# Task brief: T4-header

Goal: A new Home header: the date higher, bigger and more intricate, with a different design for each weekday (medieval planetary days); time and weather smaller and stacked on the right; everything else about 8% larger. Branch `claude/T4-header` from `claude/T3-dispatches`. Read the T1-home, T1b-polish and T3 handoffs first.

Owner decisions (`ops/decisions.md`, 2026-10-26): see the "Header redesign" line. Sunday = Sol (sun), Monday = Luna (moon), Tuesday = Mars, Wednesday = Mercury, Thursday = Jupiter, Friday = Venus, Saturday = Saturn, each with its alchemical sign and a matching ornament. Keep the C5 palette (aged gold, oxblood, charcoal) and fonts (UnifrakturCook, Cinzel, IM Fell English). The red blackletter drop cap stays as the idea, bigger.

WORK IN TWO PARTS. Part 1 needs owner approval before Part 2.
Part 1 (design preview, no Kotlin): make `docs/design/T4-header.html` (390 wide, like C5.html) showing all SEVEN weekday headers stacked (date block + small time/weather on the right), plus a full Home mock for one weekday with the other assets ~8% larger. Vector only (inline SVG, no downloaded images; drawn by you). Render it to `docs/design/T4-header.png` (use the built-in browser pane via preview_start with a file url, or any available renderer). Commit locally, then report READY FOR DESIGN OK to the coordinator with a plain-words description. STOP. Do not start Part 2 until the coordinator forwards the owner's approval or changes.
Part 2 (after approval): build it in Kotlin as new files in `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/` (header drawn with Compose Canvas / vector paths; one design per `DayOfWeek`; use the device date). Then scale the other sections ~8% (pennants, shields, orb, panels, text) keeping everything inside a 360dp-wide screen with no overlap or clipping. Small upstream edits only, separate commits.

Rules: commit locally, NEVER push or trigger a build without the owner's approval (see CLAUDE.md). Do all of Part 2 as one batch, then report READY TO PUSH with a plain-words summary. Done when: gate items 2-4 pass locally, testlist (visible result first; check all seven days via the phone's date setting steps in plain words; at least one 'should NOT happen'; check on a small and the normal screen if possible), handoff, lessons (0-3), STATUS row; build/green check only after owner OK.
Out of scope: camera pill, weather source, Treasury data, shield artwork, other redesigns.
Open questions: none now. If the design needs a choice the brief does not cover, ask the owner multiple choice.
