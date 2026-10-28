# Verified facts (add AF-n entries; mark UNVERIFIED until tested on the phone)


## Added 2026-10-27 (coordinator, from threads T1 to T16 and the owner's phone tests on the Galaxy S20 FE, Android 13)
VERIFIED on the phone:
- AF-1 Notifications: Kvaesitso's own NotificationListenerService plus `cancelNotification(key)` through its NotificationRepository work; access is granted via Android's notification-access screen (T3).
- AF-2 Weather: Bright Sky (DWD data) returns forecasts for a US location although Settings labels it "Germany only". A WorkManager job gets no live GPS without background-location permission, so the foreground header takes a fix (T4c).
- AF-3 Camera hole: on the S20 FE the display cutout outline is a box from the top edge (y=0) down past the hole; the hole is a circle as wide as the box at its bottom: centre y = bottom - width/2. `cutoutPath` bounds equal `boundingRects` here (T5c).
- AF-4 Status bar: hiding it through the scaffold config (`showStatusBar = false` route in LauncherScaffold) works; hiding it from a child composable lost to the scaffold's show-on-resume (T5).
- AF-5 Search page: `SearchComponent(openKeyboard = true)` opens the keyboard with the cursor; empty-query app list and favourites are controlled by upstream settings and were forced off in this fork (T11, T13).
- AF-6 Typing overlay: a `BasicTextField` whose value is `SearchVM.searchQuery` (`searchVM.search(text)`) shows the normal results below it (T13).
- AF-7 Home button: ACTION_MAIN/CATEGORY_HOME arrives through `SharedLauncherActivity.onNewIntent` while the launcher is on top; Samsung delivers it (picker and typing layer close, T15).
- AF-8 Quick-setting chips: `CameraManager.setTorchMode` toggles the torch with no permission; `AudioManager.setRingerMode` switches Normal and Vibrate; `Settings.System.ACCELEROMETER_ROTATION` toggles after the owner grants "Modify system settings" (WRITE_SETTINGS); Wi-Fi opens through `Settings.Panel.ACTION_INTERNET_CONNECTIVITY` (T14).
- AF-9 The 120 ms launch delay on taps lets the press reaction show; the reaction must be reset on ON_RESUME and when the motion gate closes or it can stick (T12).
UNVERIFIED on the phone:
- AF-10 Sharing text to the Claude app: `ACTION_SEND text/plain` with `setPackage("com.anthropic.claude")` (only an outside report; owner has not yet reported the result of the SUMMON test). The text is also copied to the clipboard as a backup (T6).
- AF-11 `AlarmManager.getNextAlarmClock`, AGSL RuntimeShader on API 33: not used; not tested.
- AF-12 (S5, UNVERIFIED on the phone) Apps hub: `PackageManager.queryIntentActivities(ACTION_MAIN + CATEGORY_LAUNCHER)` lists launchable apps without extra permission (QUERY_ALL_PACKAGES is already declared); `ApplicationInfo.category` is -1 for many apps. `swipeDown = null` in the scaffold config removes the pull-down gesture.
