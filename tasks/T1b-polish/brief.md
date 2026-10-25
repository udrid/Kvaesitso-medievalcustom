# Task brief: T1b-polish

Goal: Fix what the owner found on the phone after T1-home (install OK, all taps work). Branch `claude/T1b-polish` from `claude/T1-home`. Read `tasks/T1-home/handoff.md` first.

Owner decisions (2026-10-25, in `ops/decisions.md`):
1. Top rows less cluttered. Remove "Wi-Fi call" and "Voicemail" from the status line; keep Bluetooth and Hushed. Move the weather line under the clock so nothing overlaps (the screenshot shows `[weather]` on top of "Voicemail"). Keep the date line and header otherwise as C5.
2. The app picker (for replacing an app on a shield) becomes a scrolling grid of icon above name, 4 columns (5 only if names still fit on a 390dp-wide screen; pick one and state which), all installed launchable apps, alphabetical, with a search bar at the top that filters by name. Same dark vellum look and fonts as the rest. Opens from tapping an empty shield or long-pressing a filled one, as now.
3. Treasury: the owner did not ask to change it. But `[YOUR TOTAL]` currently runs into the Quest goal bar: fix only the overlap, no redesign.

Files in scope: `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/` (StatusLine.kt, HeaderSection.kt, Shields.kt, TreasuryPanel.kt, new picker file). No upstream edits unless unavoidable.
Done when: gate passes, Actions green, `tasks/T1b-polish/testlist.md` (plain steps; visible result first; a "should NOT happen" step; a restart step since picks are saved), `handoff.md`, lessons (0-3 lines), STATUS row updated, say FINISHED.
Out of scope: custom artwork for the Maps shield icon (the owner knows apps without a custom shield design show their own icon; a later task), Dispatches, camera pill, any other redesign. Do not change other layout; the owner said only "less cluttered" for the top rows.
Open questions: none. If you see a clutter fix beyond the above, list it in the handoff for the owner; do not do it.
