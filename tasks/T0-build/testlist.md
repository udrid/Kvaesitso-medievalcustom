# Testlist: T0-build (owner checks on the phone)

You are checking that the plain, unchanged launcher installs and opens. It will NOT look medieval yet. That is expected.

## A. Get the APK
1. On your phone (or PC), open the GitHub repo in a browser, tap **Actions**, and tap the newest run named "Build Medieval APK" with a green check mark.
2. Scroll to **Artifacts** at the bottom and tap **medieval-launcher-apk**. It downloads as a .zip file.
   You should see: a file `medieval-launcher-apk.zip` in Downloads. If the run is red, stop and tell me.
3. Open the zip (Samsung "My Files" > Downloads > tap the zip > Extract). If you used a PC, copy the extracted `app-default-nightly.apk` to the phone.
   You should see: a file named `app-default-nightly.apk`.

## B. Allow installing (first time only)
1. Tap the `.apk` file. If the phone says "For your security, your phone is not allowed to install unknown apps from this source", tap **Settings**.
2. Switch **Allow from this source** on (the source is the app you tapped from: Chrome or My Files). Press Back.
3. Tap **Install** again.
   You should see: "App installed". Tap **Open**.

## C. Check it works
1. The launcher asks which app should be your Home app. Choose it and tap **Always** (or open Settings > Apps > Choose default apps > Home app and pick it).
   You should see: a home screen with a search bar and apps. Pressing the Home button returns to it.
2. Open any app (for example Camera), press Home, then press the Recents button.
   You should see: the launcher still works and nothing crashes.
3. Check that **Smart Launcher** is still installed (Settings > Apps, or the app drawer).
4. Should NOT happen: the launcher closing by itself, a "keeps stopping" message, or Smart Launcher disappearing.
5. Restart check: long-press the launcher home screen, change anything small (such as the wallpaper), then restart the phone.
   You should see: the launcher comes back as Home and keeps your change.

## Report back
Tell me: (1) did it install, (2) did it open and can it be Home, (3) Smart Launcher still there, (4) any message you saw on screen.

Not verified by me: everything on this list. I cannot run the app; only your phone can.
Note: you can keep this build installed over future builds only because every build uses the same signing key. Do not uninstall it without telling me.
