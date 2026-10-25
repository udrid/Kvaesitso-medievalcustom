# Testlist: T1-home (owner checks on the phone)

You are checking the new look of the Home screen. It is called **Deliverance** now. Some parts are placeholders on purpose (see "Expected placeholders" at the end).

## A. Get and install the APK
1. On your phone open the GitHub repo in a browser, tap **Actions**, and tap the newest run named "Build Medieval APK" for branch `claude/T1-home` with a green check mark.
2. Scroll to **Artifacts**, tap **medieval-launcher-apk**, extract the zip, tap `app-default-nightly.apk`, tap **Install** (it updates the old one).
   You should see: "App installed". The app is now named **Deliverance**. Press the Home button and make it the Home app if the phone asks.

## B. The look
1. Press Home.
   You should see: a dark, grainy parchment-black screen with gold lines. At the top a big red blackletter first letter of today's day name ("S" for Sunday) and the date ("25th of October"), the clock on the right.
2. Look at the row under the header.
   You should see: "Wi-Fi call", "Bluetooth", "Hushed" on the left and "Voicemail" on the right. "Wi-Fi call" and "Voicemail" are dim on purpose.
3. Look at the four flags (pennants) in a row: VIGOR, STORES, WITS, HEAT.
   You should see: numbers. VIGOR matches your battery %. HEAT is the battery temperature. The gold fill in each flag rises with its number.
4. Look at the middle.
   You should see: a glowing round orb with a flame, a ring of Latin words around it, and "The Oracle" under it.

## C. What the buttons do
1. Tap the orb.
   You should see: the Claude app opens (empty). If it says "The Claude app is not installed", tell me.
2. Press Home, tap the four shields (left and right of the orb).
   You should see: X, Discord, YouTube, Samsung Messages open, if installed. A shield with a "+" is empty: tap it and pick an app from the list.
3. Long-press one shield, pick any app from the list.
   You should see: the shield now opens that app and shows that app's name and icon.
4. Tap the bar that says "Command the Oracle...".
   You should see: the keyboard opens with the search. Type a few letters of an app name and the app appears. Press Back to return Home.
5. Tap the four round red seals at the bottom: Herald, Portrait, Road, Smithy.
   You should see: the phone dialer, the camera, your browser, the Android Settings.
6. Toggle Bluetooth on in the phone's quick settings, then switch to silent/vibrate mode, then press Home.
   You should see (within about 5 seconds): "Bluetooth" turns bright when it is on, "Hushed" turns bright when the phone is silent or on vibrate.
7. Look at "Today's Ledger". If it says "Tap to grant calendar access", tap it and allow.
   You should see: your next two calendar events with their start time (or the weekday if not today). If you have none, it says "No tidings".

## D. Should NOT happen
1. The launcher closing by itself, a "keeps stopping" message, or the screen going blank.
2. A number badge on the shields (badges are not built yet).
3. Text cut off in the middle of the screen or on top of other text. If it is, send a screenshot.
4. Smart Launcher disappearing.

## E. Restart check
1. Long-press a shield, pick a different app, then restart the phone.
   You should see: after the restart the launcher is still Home and that shield still shows the app you picked.

## Expected placeholders (not bugs)
- Treasury shows `[YOUR TOTAL]`, `Quest: [GOAL]`, `[0%] of the way` (money comes in a later task).
- "Wi-Fi call" and "Voicemail" are dim: Android has no simple way to read them.
- No "Dispatches" box and no SUMMON button yet. No camera pill at the top yet.
- The weather line shows `[weather]` until a weather source and location permission are set in the launcher settings.

## Report back
Tell me: (1) did it install, (2) did the Home look like the design (screenshot please), (3) which taps in C worked or failed, (4) anything cut off or messages you saw.
