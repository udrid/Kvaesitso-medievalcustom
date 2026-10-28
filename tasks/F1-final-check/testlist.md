# Phone test: the combined build (landscape Home, restyled settings and search, five colour schemes)

In one line: Home now turns with the phone, every launcher page has the dark gold look, and one setting changes the colours of the whole launcher.
Takes about 15 minutes. Report each step as OK or what looked wrong (a screenshot helps: press Power and Volume down together).

## 1. Home looks as before
1. Unlock the phone and press Home, held upright.
2. You should see Home exactly as in the last build: gold and dark red, same places and sizes.
3. Should NOT happen: any part moved, a colour that looks different, the launcher closing.

## 2. Turn the phone
1. Swipe down from the top of the phone and check "Auto rotate" is lit. Go back to Home.
2. Turn the phone on its side, camera on the LEFT. You should see three columns: weekday, clock, Treasury and the four flags on the left; the orb, shields and the four wax seals in the middle; Ledger, command bar and Dispatches on the right. The parts slide into place in about a third of a second.
3. Turn it so the camera is on the RIGHT, then upright again. Same layout, moved away from the buttons; upright looks like step 1.
4. Should NOT happen: anything overlapping, anything under the camera or the navigation buttons, the top line ("DAY OF ...") cut off, a black flash or restart.

## 3. Landscape still works
1. On its side, tap the orb (Claude opens), come back, tap a shield and a wax seal: each opens its app.
2. Tap the command bar: the text field is on top, "Most used" and the switches side by side. Type "wifi", turn the phone upright and back: the text is still there. Press Home to close.

## 4. Change the colour scheme
1. Long-press the top of Home (the weekday). Settings open. At the top tap "Colour scheme".
2. Tap "Royal Blue and Silver". You should see the Settings page itself turn blue at once (ribbons, frames, wax-seal switches). Go back to Home: it is blue too, with a blue orb.
3. Try "Ember" and "Bone" the same way. Report any part that stays in the old gold, or text that is hard to read.
4. Should NOT happen: Home flickering or changing layout, the orb going black or showing a square, a colour change that needs a restart to show.

## 5. Settings, search and dialogs
1. Still in Settings, open "Search". You should see framed panels with a red (or blue) ribbon on top, and round wax seals instead of switches. Tap a seal: it slides and lights up.
2. Go back and open "Appearance". You should see only Transparency, Import, Export and "Colour source" left: the theme, colours, fonts and shapes rows are gone on purpose.
3. On Home tap the command bar and type "mar". Each group of results is one framed panel with its ribbon on top; no gold line crosses a ribbon.
4. Should NOT happen: purple or white screens, round panels, dark text on dark, a dialog whose buttons you cannot reach.

## 6. Restart
1. Pick "Bone" as the colour scheme. Open the recent-apps list, swipe Deliverance away, press Home.
2. You should see Home still in Bone (grey and white with red ribbons). Long-press the weekday: Settings are in Bone too, and "Colour scheme" says Bone.
3. Set it back to "Gold and Oxblood" (or keep the one you like).

## 7. Turn the Animations off
1. In Settings set "Animations" to Off. Go Home and turn the phone.
2. You should see the parts jump into place with no sliding. Set Animations back the way you had it.

## 8. Wallpaper, always-on display, keyboard picture and icon pack
These do not change the launcher; follow the separate steps in `tasks/S3-assets/testlist.md` (they need the icon-pack build, which is started by hand later).

## What to report
Steps 1 to 7: OK or what looked wrong, plus one screenshot of each landscape turn and one of Settings in Royal Blue.
