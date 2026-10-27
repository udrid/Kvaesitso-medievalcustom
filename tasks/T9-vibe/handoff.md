# Handoff: T9-vibe stage 2 (Part 2, build)

Part 1 (design previews and owner picks) is described in `tasks/T9-vibe/picks.md` and git history up to 28c342d99.

What changed:
- Commits: c93f10026 (orb pictures), 717890e54 (upstream: settings line), 21d3041c7 (code + testlist).
- `medieval/MedievalMotion.kt` (new): the battery gate and the setting.
  - `rememberMotion()` (provided as `LocalMotion` in `MedievalStage`): motion is allowed only when Home is resumed, the screen is interactive, Battery Saver is off, the battery is > 15 % or charging, `ValueAnimator.areAnimatorsEnabled()` is true, and the setting is not Off.
  - It also tracks charging and counts wakes (Home paused while the screen was off, then resumed).
  - The receivers (battery sticky, power-save, screen on/off) exist only between ON_RESUME and ON_PAUSE.
  - Animations setting: `AnimLevel` Full / Minimal / Off, default Minimal, in SharedPreferences `medieval_home` / `animations`.
  - Helpers: `rememberAmbientClock` (10 fps, only while allowed), `rememberOneShot` (plays once per trigger change at 10 fps, never while not allowed), `rememberTapReaction` + `Modifier.tapReaction` (250 ms gold flash and swell, or seal press), `rememberRises`, `bump`.
- `medieval/OrbGlass.kt` (new): the picked orb 2a-3. Per frame it draws:
  - `medieval_orb_smoke.webp`, turned 2.2°/s;
  - 10 twinkling dust grains (same clock);
  - the heart bloom and star, which swells on a 4.5 s cycle while charging;
  - the kindle darkening after a wake (1.1 s);
  - `medieval_orb_glass.webp` with BlendMode.Plus (fixed reflections);
  - `medieval_orb_setting.webp` (gold ring and rubies).
- The pictures (400 px, 57 KB together, `res/drawable-nodpi`) come from `docs/design/T9-orbassets.html` + `T9-orbrender.js` (rerun it in headless Edge to remake them).
- `Orb.kt`:
  - Ring text and crosses are now engraved metal: light edge +0.65, dark lip −0.45, cut colour #5E4A26, thin inner line.
  - The new `RingLight` is a sweep glint over 7 s, then 13 s rest, Full only, drawn only while visible.
  - The orb Box uses `OrbGlass` with an oxblood halo and the tap reaction.
- `Shields.kt`: tap reaction; the badge swells (1.8 s) when the app's count goes up (tracked in the shield, so the first badge also swells).
- `Dock.kt`: wax seals press in on tap.
- `Dispatches.kt`: gold halo once (1.8 s) when a newer dispatch arrives.
- `Ledger.kt`: while Home is visible and motion is allowed, every 30 s it checks for a timed event starting within 15 min; that event's red cross pulses twice (2.2 s), once per event.
- `MedievalSettings.kt` (new) + one line in upstream `MainSettingsScreen.kt` (own commit): "Animations" list at the top of the launcher settings.
- The Animations setting contents are PROPOSED by Claude (coordinator note); the owner may veto. Minimal = orb loop (smoke, twinkle, charging pulse) + one-shots + tap reactions. Full = Minimal + ring light. Off = nothing.

Why pre-rendered pictures and not a live AGSL shader (decision delegated by the coordinator):
- The orb on the S20 FE is about 375 × 375 px (124 dp × stage scale × 1.08 body scale × ~2.8 density).
- Pictures:
  - Each frame draws three bitmaps of that size, about 0.42 Mpx of fill, plus ~25 small circles and one gradient.
  - At 10 fps that is ~4 Mpx/s, a tiny fraction of an Adreno 650's fill rate.
  - CPU is one RenderNode redraw, no recomposition, about 0.3–1 ms per frame.
  - Memory is 3 × 400² × 4 B ≈ 1.9 MB.
- Live shader:
  - Every pixel would ray-march 24 steps with about 13 value-noise evaluations each, ~44 M noise evaluations per frame (~350 M hash operations).
  - At 10 fps that is a sizeable continuous GPU load, estimated 5–10× the power of the bitmap approach.
  - AGSL is also UNVERIFIED on this phone (no AF entry, no probe).
- Pictures are cheaper and have fewer unknowns, so pictures were built.
- The cost of the look: the smoke turns as a whole instead of truly swirling.
- Probe idea if the owner wants true swirling later: a tiny RuntimeShader test screen measuring frame time; or 24–48 pre-rendered frames (~1–2 MB more RAM).
- Battery estimate (not measured): about 1–3 % extra per hour of screen-on time on Home with Minimal. Part of it is that any animation keeps the display composing (and possibly at 120 Hz) instead of idling. 0 with Off or Battery Saver.

How it was checked:
- `./gradlew :app:ui:compileDebugKotlin --offline` (Android Studio JBR): exit 0, no errors.
- The orb pictures were stacked in a browser and compared with the picked design (they match).

NOT verified:
- Nothing has run on the phone: looks, frame rate, battery, settings screen.
- Assumptions in the code:
  - `PowerManager.isInteractive` is false at ON_PAUSE / ON_STOP when the screen turns off; the kindle depends on it.
  - System broadcasts (battery, power-save, screen) arrive with `RECEIVER_NOT_EXPORTED`; same pattern as the pill's Hushed receiver.
  - `ImageBitmap.imageResource` decodes the webp files from drawable-nodpi.
  - `BlendMode.Plus` on the glass layer looks like the preview.
- Battery numbers are estimates.
- No `api-scout` check was run (no AGSL, no new Android APIs beyond common ones).

Known gaps / follow-ups:
- **New-day shimmer of the red capital (owner picked) is NOT built.** It lives in `HeaderSection.kt`, which T10 is changing (coordinator rule). After T10 merges, pass a `rememberOneShot(date, 1600, LocalMotion.current.ambient)` to the capital and draw a brief gold-leaf brightening.
- **The weather stop-motion (T4, `HeaderSection`/`VectorCanvas`) ignores the new gate.** It keeps running at about 12 fps even with Off or Battery Saver, and with the orb loop it makes two ambient effects in Minimal and three in Full (rule: at most two). Fix after T10: run it only when `LocalMotion.current.ambient`, and decide with the owner whether Full keeps the ring light, the weather, or both.
- `rememberNow()` (MedievalParts, pre-T9) loops every 5 s forever, even when Home is stopped. This is not T9 work, but it is worth a lesson.

Next stage needs:
- Owner approval to push, a green CI build, the owner running `tasks/T9-vibe/testlist.md`, then a fix thread for any bugs.
- The owner should confirm or change the Animations setting contents.
