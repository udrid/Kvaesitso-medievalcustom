# Task brief: T3-dispatches

Goal: The "Dispatches" box from C5 shows the phone's real notifications with "N unheeded", and the four shields show a number badge for their app's unread notifications. Branch `claude/T3-dispatches` from `claude/T1b-polish`. Read `tasks/T1-home/handoff.md` and `tasks/T1b-polish/handoff.md` first.

Owner decisions: notification access, yes, owner grants it once (`ops/decisions.md`). Look is C5 (`docs/design/C5.html` has the Dispatches box and the badge style). Do not redesign.

Files in scope: new files in `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/` (Dispatches list, badge), a NotificationListenerService (check whether Kvaesitso already has one in `services/` or `data/notifications` and reuse it before writing a new one), small manifest/settings edits in separate commits. `tasks/T3-dispatches/testlist.md`, `handoff.md`.

Build in this order, one commit each:
1. Permission flow: if notification access is off, the Dispatches box shows a line "Tap to grant notification access" that opens the Android notification-access screen. Plain words, no jargon.
2. Dispatches list from active notifications: app icon, title, short text, time, newest first, "N unheeded" header count. Placeholder-free: if none, "No dispatches".
3. Tap a dispatch opens its app/intent.
4. Shield badges: count of active notifications per shield app (the picked app, not hardcoded Discord/Messages). Hide when 0.

Verify listener and notification calls with `api-scout` first (UNVERIFIED facts get an `ops/api-facts.md` proposal in lessons-inbox, you do not edit api-facts.md).

Done when: gate passes, Actions green, testlist (visible result first; at least one "should NOT happen"; a restart step; how to grant access in plain steps; how to make a test notification, e.g. send yourself a message) and handoff written, STATUS row updated, say FINISHED.
Out of scope: camera pill, weather, Treasury, shield artwork, changing other layout.
Open questions (ask the owner, multiple choice, before step 3): can a dispatch be swiped away/cleared, or tapping only opens the app? Offer: A) tap opens only, B) swipe clears it from the list only, C) swipe dismisses the real notification. Ask before building clearing.
Notes: never log notification text (privacy); log only tags/errors with `medieval`.
