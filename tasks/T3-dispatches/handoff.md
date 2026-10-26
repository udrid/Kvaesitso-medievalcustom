# Handoff: T3-dispatches stage 1

What changed (one commit per step, branch `claude/T3-dispatches`):
- Reused Kvaesitso's `NotificationService` (data/notifications) and `NotificationRepository`; no new listener, no manifest change. Permission state and the opener of Android's notification-access screen come from `PermissionsManager` (`PermissionGroup.Notifications`).
- Step 1: `Dispatches.kt` (new) fills the empty panel at y=612 (ribbon "Dispatches"). With access off it shows "Tap to grant notification access" which opens the Android screen. `MedievalHomeVM` got `hasNotificationAccess`.
- Upstream fix (own commit, `Notification.kt`): the constructor used after a ranking update dropped `flags`, which would have made group-summary detection fail. One line.
- Step 2: list of active notifications, newest first, top 3 rows: app icon, "title · text", time (weekday if not today), red "N unheeded" (counts all dispatches, not only the 3 shown), "No dispatches" when empty. A notification counts only if it is clearable, not a group summary and has title or text. Title/text read with `getCharSequence` (upstream `Notification.title` uses `getString`, which gives null for styled text). Row gap 7 instead of 9 so 3 rows fit the 90 high box.
- Step 3 (owner chose option C): tap = send the notification's PendingIntent (falls back to launching the app); swipe left/right past 90 px = real `cancelNotification` through the repository.
- Step 4: `Badge` in `Shields.kt` (C5 `.badge` style) with the count of dispatches whose package is the shield's current app; hidden at 0. `Shields(vm)` now takes the VM.
- No notification text is logged. No new `catch` without a `medieval` log.

How it was checked:
- GitHub Actions build for `claude/T3-dispatches`: see STATUS row for the run id and result.

NOT verified (workspace cannot run the app):
- Anything on the phone: look and fit of 3 rows in the box, badge position, text lengths and ellipsis.
- That Samsung's notification-access screen lists Deliverance, and that the service connects right after granting (some phones need the app restarted once).
- That `PendingIntent.send()` opens the right screen of each app from the launcher on Android 13.
- That the horizontal swipe is not stolen by an upstream Kvaesitso gesture on the home screen, and that cancelling works for every app.
- Notifications that arrive while the launcher is closed show up after the next start (the service is system-bound; unproven on this phone).
- Which notifications Samsung marks non-clearable (hidden from Dispatches by design).
- Samsung Messages notifications may be grouped/summary-only on some versions; badge count could then read 0.
- API facts used are standard Android (`NotificationListenerService`, `cancelNotification`, `ACTION_NOTIFICATION_LISTENER_SETTINGS`) and were already used by Kvaesitso; no AF entries exist, `api-scout` was not run (see lessons).

Next stage needs: nothing. The camera pill, weather and Treasury remain separate tasks.
