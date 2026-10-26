# Owner checks on the phone: T3-dispatches

Install the new APK from the GitHub Actions run for branch `claude/T3-dispatches` (same way as before; it installs over the old Deliverance).

## A. Give the launcher permission to see notifications (once)
1. Open Deliverance (the home screen). Look at the "Dispatches" box (below the command bar, above the dock icons).
   You should see: "Tap to grant notification access".
2. Tap that line. An Android screen called "Notification access" opens.
   Find "Deliverance" in the list, tap it, and turn it ON. If Android shows a warning, read it and tap Allow.
3. Press the Back button until you are on the home screen again.
   You should see: the line is gone. The box says "No dispatches" (or lists your notifications).
   Report: did the line vanish, or did it stay?

## B. Make a test notification and see it
1. Send yourself a message: ask a friend to text you, or use another app to message yourself (for example Discord or Samsung Messages). Leave the message unread.
2. Go to the Deliverance home screen.
   You should see in the Dispatches box: the app's small icon, the sender or title, a short piece of the message, and the time at the right. Newest on top. Top right of the box says "N unheeded" in red (N = how many).
3. Look at the four shields. If one of them is the app you got the message in (for example Discord or Messages), a small red round badge with a number sits on its top right corner.
   Report: the number in the badge and the number in "N unheeded", and what each row shows.

## C. Tap and swipe
1. Tap a dispatch row.
   You should see: the app opens at that message (or at least the app opens).
2. Go back to home. Swipe a dispatch row to the left or right and let go.
   You should see: the row slides away; the "N unheeded" number goes down by one; the badge on the shield goes down by one (or disappears at 0). Pull down the Android notification bar: that notification is gone there too.
3. Swipe a row only a little bit and let go.
   You should see: the row springs back and nothing is deleted.

## D. Things that should NOT happen
1. No badge should show on a shield when its app has no notification (zero must show nothing, not "0").
2. A music player or "app is running" notification (the kind you cannot swipe away in the Android bar) should NOT appear as a dispatch.
3. No notification text should be shown anywhere except inside the Dispatches box.
4. The launcher should not crash or freeze when you get a notification while it is open.

## E. Restart check
1. Swipe Deliverance away from the recent-apps list (close it fully), then open it again.
   You should see: your unread notifications are listed again and the badges are back, without you doing anything. The notification access stays ON.
2. Change a shield to another app (long-press a shield, pick an app that has an unread notification).
   You should see: the badge now shows that app's count.

## F. Report back
Tell me: what you saw at A3, B2, B3, C1, C2, and anything that looked cut off, overlapped or wrong (the three rows must fit inside the box).
