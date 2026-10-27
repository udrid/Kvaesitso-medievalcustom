# Handoff: T12-orb-tune (stage 1)

What changed (all in `app/ui/.../ui/medieval/`):
- `OrbGlass.kt`: smoke turn 2.2 -> **0.8 deg/s** (one full turn in 7.5 min). Chosen because the heart, glass and dust still read as alive (twinkle carries the life) while the turn is hard to notice as spinning. Orb clock **10 -> 5 fps** (`OrbFps`). Reason: 0.8 deg/s is 0.16 deg per frame, about half a pixel at the rim; a twinkle period is ~4.8 s, so 0.2 s steps are smooth. A separate faster twinkle layer was NOT needed.
- `MedievalMotion.kt`: `rememberAmbientClock(running, fps)` got an fps parameter (other callers keep 10). Tap reaction rewritten (see below). All T9 gates unchanged (resumed, screen on, Battery Saver off, battery > 15 % or charging, remove-animations off, setting not Off).
- `Orb.kt`: "The Oracle" label removed, nothing else moved, orb unchanged in position.
- `Dock.kt`, `Orb.kt`, `Shields.kt`: apps now open through `tap.open { ... }`.

Why the tap reactions were never seen (root cause, from code):
- The reaction used to start on `PressInteraction.Release`, and `clickable` runs its `onClick` on that same release. The app launch happened in the same instant, so the activity switch started before the first reaction frame (250 ms animation, peak at 75 ms). The reaction code did run when motion was allowed, but nobody could see it.
- Fix: the reaction starts on `Press` (90 ms to full), stays while held, fades 160 ms on release/cancel (`collectLatest`). `TapReaction.open(action)` waits **120 ms** before launching while motion is allowed, and launches at once otherwise (setting Off, Battery Saver, low battery, remove-animations). A second tap during the wait is ignored. The command bar is untouched. Shield long-press (picker) and the empty-shield pick open at once.

Battery estimate (NOT measured):
- Orb is about 375 x 375 px on the S20 FE = 0.14 Mpx per layer. Per frame: rotated smoke + glass + setting = 3 layers (0.42 Mpx) + heart gradient + up to ~20 tiny circles, about 0.5 Mpx.
- Redraws: 5 per second (was 10). Fill: 0.5 x 5 = 2.5 Mpx/s (was 5). The Adreno 650 does several thousand Mpx/s, so this is well under 0.1 % of its capacity.
- CPU: one RenderNode redraw, about 0.3-1 ms per frame, so 1.5-5 ms per second = 0.15-0.5 % of one core.
- The larger cost is waking the display pipeline 5 times a second instead of letting it idle: it costs a bit extra per composed frame. My estimate is **about 0.5-1 % of battery per hour of screen-on time on Home** (earlier estimate at 10 fps and faster turn: 1-3 %). Zero in Off, Battery Saver, screen off or when another app is in front (nothing runs when Home is not resumed). The screen itself uses far more than this.
- Full also runs the ring light (10 fps for about 7 s of every 20 s); the default Minimal does not.
- Skipping redraws: the clock does not run at all when not allowed; when it runs, every frame changes the picture (turn and twinkle), so there is nothing more to skip without visible steps.
- Owner check after a day: see testlist step 7 (Settings > Battery > Battery usage, find Deliverance).

How it was checked: `./gradlew :app:ui:compileDebugKotlin --offline` exit 0.

NOT verified: everything on the phone. Looks of the slow turn, smoothness at 5 fps, that the press flash is visible while holding, that the 120 ms delay feels like a blink, the battery numbers (estimates). Assumed: `clickable` delivers `PressInteraction.Press` immediately on touch-down (it does after the tap-timeout only inside scrolling containers; the dock/shield/orb are not scrolled, but if Home sits in a scroll container the press may arrive ~100 ms late; then raise the delay or test).

Follow-ups left alone (out of scope): weather stop-motion ungated and the new-day shimmer (see T9 handoff).
