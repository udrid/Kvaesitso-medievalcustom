# T9-vibe: owner picks so far

- 2026-10-26, orb: **2a-3 "Heavy gold dust"** (`docs/design/T9-orb11.html`, look id `d`).
  - Oxblood smoke in glass, about three times the gold dust, a soft highlight up-left (no window cross), faint candle glint right.
  - Gold setting: shaded band, beaded inner edge, twisted-rope rim, engraved scrolls, four rubies.
  - Same size as today (the larger-orb option was not picked).
  - Renderer parameters: `X({wm:'soft',dust:.017,seed:23.1})`, sparks `('#ffd58a'→'#ffd890',18,11,8,50)`, flare `#ffa060`.
- Still open: ring text style (`T9-ring.html`: 0 / 1 / 2 / 3 / 3b) and which animations (`T9-vibe.html`).
- How to build the orb on the phone is not decided: pre-drawn frame loop (low cost) or a live GPU shader (unverified on the phone).
