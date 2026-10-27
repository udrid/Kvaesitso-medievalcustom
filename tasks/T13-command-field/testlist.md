# Phone test: T13-command-field (type in the Command bar)

What changed in one line: tapping "Command the Oracle..." now opens a screen whose top bar is a real typing box (keyboard up, cursor blinking), with your phone's search results under it, and SUMMON sends exactly what you typed.

1. Tap the bar
   - Be on the Home screen. Tap the bar "Command the Oracle..." (leaf icon, SUMMON on the right).
   - You should see: the Home fades out, the same kind of bar sits at the top of the screen, the keyboard opens and a gold cursor blinks INSIDE the bar. The bar is not hidden by the keyboard. Nothing is listed under it yet.
   - Report: yes/no, and whether it looks like the Home bar.
2. Search
   - Type `cam`.
   - You should see: results under the bar (for example Camera), as in the normal search. Tap the small cross in the bar: the text vanishes and the list vanishes.
3. Ask the Oracle
   - Type `What is a good name for a cat?` and tap SUMMON.
   - You should see: a short message ("Copied. Paste if needed."), then the Claude app opens with that exact sentence in a new message box (UNVERIFIED: if the box is empty, long-press the box and paste). Tap Send in the Claude app yourself.
   - Report: what the Claude app showed (text there? empty? old chat?).
4. Come back
   - Switch back to Deliverance (Home button or Recents).
   - You should see: the bar still holds your sentence.
5. Back
   - Press Back. If the keyboard is open, the first Back only hides it; press Back again.
   - You should see: the typing screen closes, Home is back, the bar shows "Command the Oracle..." again. Tap the bar again: it is empty.
6. SUMMON with nothing typed
   - On Home, tap SUMMON (right end of the bar) with the bar untouched.
   - You should see: only the message "Write thy question first." and nothing else happens.
7. Open an app from a result
   - Tap the bar, type `cam`, tap Camera. The camera opens. Return to Home.
   - You should see: the bar is empty again.
8. Restart (the typed text is not saved by design)
   - Type `abc`, then swipe the launcher away from Recents and open Deliverance again.
   - You should see: Home as normal, tapping the bar shows an empty box.

Should NOT happen
- A list of all your apps before you type anything.
- SUMMON sending older text than what is in the bar.
- The text you typed disappearing when the results appear.
- The keyboard covering the bar or the results being unreachable behind it.
- Back leaving the text behind after the screen closes.
- The normal search page (swipe) behaving differently: swipe to open it with text typed in the bar and it should show the same text.
