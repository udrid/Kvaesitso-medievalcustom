# Task brief: T13-command-field

Goal: the Home bar "Command the Oracle..." is a real text field. Tap: cursor in the bar, keyboard up. Typing shows the launcher's normal search results (apps, contacts, calendar, settings, web), nothing before typing. SUMMON sends exactly the typed text (copy + share to the Claude app, T6) at any time; empty text = hint only. Text stays until Back/clear. One source of truth for the text: `SearchVM.searchQuery`.
Owner report (phone, 2026-10-27): "the summon bar opens up the launcher's search and I can search my phone; no text is carried into that bar."
Files in scope: `app/ui/.../ui/medieval/` (CommandBar.kt, new CommandOverlay.kt, MedievalHomeComponent.kt). Upstream edits only if unavoidable, tiny, separate commits. Search behaviour may change on Home only.
Done when: compile green; `testlist.md`, `handoff.md`, 0-3 lessons lines, STATUS row; report READY TO PUSH with plain-words summary and what was NOT verified.
Out of scope: other screens' search, redesign of C5, pushing/building without owner yes.
Open questions: none (owner allowed changing Home search behaviour).
