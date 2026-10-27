# T9-vibe: owner picks so far

- 2026-10-26, orb: **2a-3 "Heavy gold dust"** (`docs/design/T9-orb11.html`, look id `d`).
  - Oxblood smoke in glass, about three times the gold dust, a soft highlight up-left (no window cross), faint candle glint right.
  - Gold setting: shaded band, beaded inner edge, twisted-rope rim, engraved scrolls, four rubies.
  - Same size as today (the larger-orb option was not picked).
  - Renderer parameters: `X({wm:'soft',dust:.017,seed:23.1})`, sparks `('#ffd58a'→'#ffd890',18,11,8,50)`, flare `#ffa060`.
- 2026-10-26, ring text: **2. Engraved metal** (`docs/design/T9-ring.html`).
- 2026-10-26, animations: **gold dust twinkle, tap reactions, notification glow, light drifting round the ring** (plus the orb's slow smoke loop). The owner also asked for proposals of other animations and graphics the Home needs: `docs/design/T9-vibe2.html`.
  - Battery rule check: smoke loop + dust twinkle + ring light would be three ambient effects. Proposal: bake the twinkle into the smoke loop frames, so the orb counts as one and the ring light as the second.
- How to build the orb on the phone is not decided: pre-drawn frame loop (low cost) or a live GPU shader (unverified on the phone).
