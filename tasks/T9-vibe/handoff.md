# Handoff: T9-vibe stage 1 (Part 1, design previews)

What changed:
- Design files only, no Kotlin. Under `docs/design/`:
  - `T9-ring.*`: ring text styles.
  - `T9-vibe.*` and `T9-vibe2.*`: animation menus.
  - `T9-orb*.html` / `.png` / `.view.html`: eleven rounds of orb concepts.
  - `T9-orb.js`: ring-text and orb helper.
  - `T9-orbrender.js`: the per-pixel glass-orb renderer used for the picked orb.
- Owner picks are in `tasks/T9-vibe/picks.md`:
  - Orb **2a-3 "Heavy gold dust"** (oxblood smoke, gold dust, soft highlight with no window cross, gold setting with rubies, same size as today).
  - Ring text **2. Engraved metal**.
  - Animations: orb smoke loop with gold-dust twinkle, light drifting round the ring, tap reactions, notification glow, orb kindles on wake, new-day shimmer of the red capital, Ledger red cross pulses before an event.
- Battery-rule note: the twinkle must be baked into the orb's smoke loop, so the ambient effects are orb (1) + ring light (2). The brief allows two.
- Proposal, not yet an owner decision: Animations setting Minimal = orb loop + one-shots; Full = also ring light; Off = nothing.

How it was checked:
- Every page was rendered with headless Edge and the PNGs were inspected.
- `T9-vibe2.html` runs in Edge (the owner viewed it). The app's browser pane only shows the frozen `.view.html` copies.

NOT verified:
- Nothing has been run on the phone.
- The motion was never watched by me (only stills).
- Battery figures are estimates.
- How the orb is drawn on Android is open. Two options:
  - (a) a pre-rendered loop of 24 to 48 frames at about 124 dp, played at 8 fps;
  - (b) a live AGSL RuntimeShader (API 33, Android 13). This is UNVERIFIED: no AF entry. Ask `api-scout` / probe first.
- Larger-orb room figures come from the C5 mock numbers, not the real Home (not picked anyway).

Next stage needs:
- Part 2 from the brief, building only the picks above, in new files under `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/`:
  - follow the battery rules;
  - add the "Animations: Full / Minimal / Off" setting (default Minimal; its contents need an owner OK);
  - compile, then write testlist, handoff, lessons and the STATUS row;
  - report READY TO PUSH.
- Opus is suggested for planning the orb rendering approach (new technique).
- Port the maths in `T9-orbrender.js` (the `renderOrb` and `env` functions, look id `d` in `LOOKS`).
- Engraved ring style: `STYLES.engraved` in `T9-orb.js`.
