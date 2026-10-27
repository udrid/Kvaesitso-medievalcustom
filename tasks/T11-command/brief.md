# Task brief: T11-command

Branch `claude/T11-command` (worktree), cut from `claude/integrate-1`.
Bug (owner, 2026-10-26): on Home, tapping "Command the Oracle..." opens the launcher search page, which first shows a long list of all apps, and the owner could not type a question (so SUMMON could not be tried).
Owner allows changing this behaviour (only the Home command bar and its search; nothing else phone-wide).

Goal: the Home bar becomes a real text field: tap = cursor + keyboard at once (Home itself, or the search page opened with keyboard up and cursor focused). NO all-apps list before typing (nothing, or a short hint). Typing shows the normal results (apps, contacts, calendar, settings, web). SUMMON sends exactly the typed text (T6: copy + share) from the Home bar and the search page. Back clears and closes.
Smallest change first: find why focus/keyboard fail and why the app list shows (search settings, SearchVM, SearchColumn). Upstream edits small, own commits.
Done: compile green (`./gradlew :app:ui:compileDebugKotlin --offline`), testlist.md, handoff.md, lessons (0-3), STATUS row, READY TO PUSH. Never push. Say what was not verified.
