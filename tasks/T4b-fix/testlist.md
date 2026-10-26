# Owner checks on the phone: T4b-fix

Install the new APK from the GitHub Actions run for branch `claude/T4b-fix` (it installs over the old Deliverance).

## A. Layout
1. Press the Home button so you see the Deliverance home screen.
   You should see: the whole screen sits ABOVE the three Android buttons at the bottom. The four wax seals and their names (HERALD, PORTRAIT, ROAD, SMITHY) are fully visible with a gap under them.
   You should NOT see: any dock name hidden behind or touching the Android buttons.
2. Look at the four small banners (VIGOR, STORES, WITS, HEAT) under the Treasury box.
   You should see: the left edge of the first banner lines up with the left edge of the Treasury box, the right edge of the last banner lines up with its right edge, and the gaps between the four are equal.
3. Look at the whole screen from top to bottom.
   You should see: left and right margins look equal everywhere (header, Treasury, banners, orb, shields, Ledger, Dispatches, dock). The four seals are spread evenly.
   Report: anything that still looks shifted, with which row it is.
4. Look at the top.
   You should see: the weekday line sits just under the Android clock and battery row, not touching it.

## B. Weather setup (one time)
1. On the Home screen, touch the big weekday name at the top (for example "Monday") with one finger and keep holding for about one second.
   You should see: the Deliverance settings open (a list of settings in the usual Android look).
2. In that list tap "Integrations", then tap "Weather".
   You should see: a screen with "Weather provider" and "Location".
3. Tap "Weather provider" and choose "Bright Sky" (or "MET Norway"; they need no account). Then switch on "Automatic location".
   You should see: a popup asking for location permission. Tap "While using the app", then "Precise" if asked.
4. Press Back until you are on the Home screen. Wait up to one minute.
   You should see: a temperature and word (for example "14° · Cloudy") under the clock, and the little landscape picture.
   Report: what the weather line says and whether the picture matches.
5. If the weather line still says "Tap to set up weather", tap it.
   You should see: the weather settings open directly. Report what the screen shows under "Location".

## C. Should NOT happen
1. Tap the weekday name quickly (do not hold).
   You should NOT see: settings open. Only holding opens them.
2. With weather working, the words "Tap to set up weather" should NOT be there.

## D. Restart check (settings are saved)
1. Open Android Settings > Apps > Deliverance > Force stop, then press Home.
   You should see: the weather line is still there after a minute, and you did not have to set it up again.
