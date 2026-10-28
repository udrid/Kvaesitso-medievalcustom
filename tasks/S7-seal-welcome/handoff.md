# Handoff: S7-seal-welcome (Part 2, build)

Owner pick (2026-10-28): variants B and C combined, about 1.5 s, plays on every unlock into Home, setting "Welcome animation On/Off" (default On) next to Animations. Part 1 preview: `docs/design/S7-seal-welcome.html/.png`.

What changed (commits 25f2f8c94 upstream settings line, ea543b4da code, then tests/docs):
- `medieval/MedievalWelcome.kt` (new): everything.
  - `Welcome` clock (`ms`, -1 = not playing), `rememberWelcome(motion)`, timeline, drawing of the sigil and the seal halves.
  - Trigger: `Motion.wakes` (T9: Home paused while the screen was off, then resumed) -> check setting On, motion gate open, `KeyguardManager.isKeyguardLocked`. If the keyguard is still locked: wait (max 60 s) for `ACTION_USER_PRESENT` (receiver only while waiting) or the flag turning false (polled every 200 ms). Then a 120 ms hold (frame 0: sections down, orb dark, seals whole) and the 1500 ms show at 25 fps (40 ms steps). Total at most 1.62 s.
  - Not on later Home presses: the trigger is the wake counter only. Not when you come back to Home from another app.
  - Safety: tap anywhere (first-pass pointer input, never consumed) ends it; gate closing, Home pausing, an exception or a 2.6 s timeout all end it. Ending = `ms = -1` = everything drawn normally. Alpha never blocks touches.
  - Log (tag medieval, no coordinates): wake number, keyguard locked yes/no, which signal unlocked, finished / timed out / gate closed.
- `MedievalLandscape.kt` `RotateGlide.Section`: sections fade and lift in order (Header, Treasury, Pennants, Shields and Forecast, Ledger, Command, Dispatches) read in the graphics layer only (no recomposition). Orb zone and dock section are not faded.
- `MedievalHomeComponent.kt`: creates the welcome, provides `LocalWelcome`, tap-to-skip, `ShieldsRising` wrapper.
- `Dock.kt`: seal halves drawn over each seal (crack, split, fall, ember chips); divider and labels rise.
- `Orb.kt`: the sigil canvas around the ring. `OrbGlass.kt`: while the welcome plays it sets the orb's darkness (dark until 650 ms, alive at 1400 ms); otherwise the T9 wake kindle runs as before.
- `MedievalSettings.kt` + one line in upstream `MainSettingsScreen.kt` (own commit): "Welcome animation".
- Test: `S7Shots` (Robolectric) renders frames 0/300/600/900/1200/1500 in Gold and Ember, the normal Home, and a tap at 600 ms (asserts the welcome ends). Pictures in `docs/design/shots/S7/` (looked at; the orb is dark in the early frames, the sigil is closed at 900, the tap picture equals the normal Home).

Battery (estimate, not measured): 1.5 s at 25 fps once per unlock, small regions plus section layers; about 0.05 to 0.1 % of the battery per day at 100 unlocks. Nothing runs between unlocks (the waiting loop exists only while the keyguard is still up).

Changed from the preview: sigil radius slightly smaller (91 vs the ring 86) so it clears the pennants.

NOT verified (phone only):
- That Home really resumes AFTER the unlock (not at screen-on behind the lock screen) on One UI 5; the T9 wake flag depends on `isInteractive` being false at ON_PAUSE. If Home resumes behind the keyguard the code waits for USER_PRESENT; if USER_PRESENT is not sent the 200 ms flag poll covers it. The log lines tell which path ran.
- Feel and timing next to Samsung's unlock animation, the hold of 120 ms, 25 fps smoothness.
- Landscape: the code covers it (same section wrapper) but no landscape picture was rendered.
- Real battery cost; with the Animations setting Off the show never plays (by design).
