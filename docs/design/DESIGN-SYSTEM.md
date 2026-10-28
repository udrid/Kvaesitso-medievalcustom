# Deliverance design system (from the owner-approved C5 design and rounds T1 to T17)

Source of truth for every new screen. Colours and fonts live in code at `app/ui/src/main/java/de/mm20/launcher2/ui/medieval/MedievalTheme.kt` (object `Medieval`). Reference renders: `docs/design/C5.png`, `T4-header.png`, `T7-shields.png`, `T9-orb*.png`, `T14-layer.png`. Mark: [M] = a part that can be monetised (see `ops/monetization.md`).

## Look
Kingdom Come: Deliverance inspired, dark edition (inspired by only: never copy the game's logos, art or name). Charcoal vellum, aged gold, oxblood, film grain and a dark vignette over everything.
- Colours: Gold C9A55A, GoldDeep 8C6D3A, GoldDark 6B5430, GoldLine 4A3A20, Text E6D9B8, TextBright F2E6C6, TextDim A89A76, Oxblood 6A1D16, Rubric A8352B, PanelTop 1D1A14, PanelBottom 12100C, Ink 0A0907, Ember F0A050, Parchment ECDFBF.
- Fonts (all SIL OFL, licences in `docs/licenses/fonts`): UnifrakturCook (blackletter headings), UnifrakturMaguntia (weekday names and drop cap), Cinzel (labels and the ring text, Bold for engraved text), IM Fell English (body, italic placeholders). [M] bundling OFL fonts in a paid app is allowed; selling the fonts by themselves is not.
- Panels: double gold hairline frame on a vertical charcoal gradient, red ribbon heading with swallow-tail (Cinzel caps). Pennants (swallow-tail flags), heater shields, oxblood wax seals.
- Emblems: gold line art on the shield field, medieval objects (raven, goblet, scroll, sealed letter, compass); apps without an emblem get a gold letter shield, never a coloured app icon. [M] emblem set and its packaging as an icon pack.
- Weekday system: seven planetary days (Sol, Luna, Mars, Mercury, Jupiter, Venus, Saturn), each with a medallion, ornament and divider. [M] the seven-day header set as a theme or wallpaper pack.
- Orb: glass orb with oxblood smoke, gold dust, gold setting with rubies; engraved ring text NOSCE TE IPSUM, SAPERE AUDE, LUX ET VERITAS. [M] orb art and ring.

## Rules for any new screen
1. Reuse the pieces above; invent a new motif only after asking the owner (multiple choice, with previews).
2. Layout: Home is drawn on a 390 x 844 stage scaled to the screen, inside the system bars. Sizes in stage units.
3. Motion (hard rules, `MedievalMotion.kt`): animate only while the launcher is resumed, the screen is on, Battery Saver is off, battery above 15% or charging, Android's remove-animations is off, and the Animations setting (Full/Minimal/Off) allows it. Small regions only; ambient effects at 12 fps or less (orb 5 fps); at most two ambient effects; reaction effects start on press and must always reset (ON_RESUME, gate closing).
4. Taps: dock, shields and orb launch about 120 ms after the press flash, instantly when animations are off.
5. Text on dark grounds: warm parchment tones, never pure white; keep contrast readable at 10 sp for small caps.
6. Never show invented personal data (Treasury is placeholders until the owner connects data). Never log notification text or coordinates.
7. Every phone-only fact goes to `ops/api-facts.md` as VERIFIED or UNVERIFIED.

## What the launcher can and cannot style
Can (we draw it): Home, the typing layer and search results, launcher settings, widget page, sheets and dialogs, app picker, icon shapes and icon pack, wallpaper (a launcher can set one).
Cannot (stock Samsung One UI 5, locked bootloader): the Android quick panel, lock screen, always-on display, other apps' insides, the system navigation bar. Partly reachable with Samsung's Good Lock (Theme Park, LockStar, ClockFace, QuickStar, NavStar) and SwiftKey themes: we produce the images and steps, the owner applies them.

## Screen inventory (status)
| Screen | Status |
|---|---|
| Home portrait | DONE, owner verified |
| Typing layer and search results | layer DONE; result cards still stock look |
| Launcher settings | stock look (only the Animations entry is ours) |
| Widget/feed page | not styled |
| Sheets, dialogs, item cards | not styled |
| Home landscape | not built (rotation is locked to portrait by an upstream setting) |
| Wallpaper, lock screen, AOD | not made |
| Icon pack | not made |
| Keyboard (SwiftKey) theme | not made |
