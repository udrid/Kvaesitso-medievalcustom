# Monetisation notes (owner request 2026-10-27: note anything that can be monetised)

Not legal advice. Everything marked CHECK must be verified with a lawyer or the licence text before selling anything. The owner decides what to sell and when.

## What we have and how it could earn
| Part | Where | Licence / ownership | Ways to monetise | Risks / CHECK |
|---|---|---|---|---|
| The launcher app itself ("Deliverance") | whole repo | A fork of Kvaesitso, GPL-3.0: derived code must stay GPL-3.0, and anyone who gets the app gets the source | Sell the APK or a Play Store listing (GPL allows charging); paid support; donations; a paid "supporter" build | Must publish source for what is distributed; it cannot be made closed. Change the applicationId and the upstream app icon (credited to EliotAku in the readme) before publishing; changing applicationId forces a reinstall for current users. CHECK the app name "Deliverance" against the game's trademarks; do not use the game's logos, art or name |
| Original artwork: emblems, seven weekday headers, medallions, orb, ring, wax seals, letter shields | `app/ui/.../medieval/`, `docs/design/` | Made by Claude for the owner in this project; the owner holds the rights under Anthropic's terms (CHECK current terms). Art that ships inside the GPL app is distributed under GPL | Sell as SEPARATE products under their own licence: an icon pack app, wallpaper and lock-screen packs, theme files, printable posters | Art that exists only inside the GPL app is GPL; keep sellable art as separate files or a separate repo with your own licence |
| Icon pack (S3 preview made 2026-10-27; Part 2 after owner picks) | `docs/design/S3-assets.html`, later a separate module | Own art, separate files | Paid icon pack on Play Store (US$25 one-time developer fee); bundle with the launcher; seal-plate and shield-plate variants as two products [M] | Needs about 120 emblems for good coverage; the pack format cannot draw letter shields (only wraps the real icon); do not use the game's marks. CHECK store policy and name |
| Wallpaper, lock screen and AOD set (S3 preview made) | `docs/design/S3-assets.html` | Own art | Paid pack: seven planetary-day wallpapers (1B), the orb wallpaper (1C), seven AOD day images (2B) [M]; free single image as a promo | Good Lock steps for users to apply; Good Lock availability UNVERIFIED on a US S20 FE |
| Launcher theme files | Kvaesitso can import themes (`ImportThemeActivity`) | Own | Sell theme packs | CHECK the theme file format licence |
| SwiftKey keyboard theme (S3 preview made) | one PNG + colour card | Own | Free promo only (SwiftKey themes are one picture plus presets, little to sell) | none |
| Fonts | UnifrakturCook, UnifrakturMaguntia, Cinzel, IM Fell English | SIL OFL 1.1, licences kept in `docs/licenses/fonts` | May ship inside paid products | The fonts cannot be sold alone; keep the licence files |
| The phone-stylist agent and the workflow (`.claude/agents`, CLAUDE.md rules, thread templates) | repo | Own | Sell as a guide, template or course: how to build a themed launcher with Claude Code | Remove personal data (email) before sharing |
| Treasury panel (placeholders now; SimpleFIN later) | `TreasuryPanel.kt` | GPL code | A paid add-on feature | Financial data: SimpleFIN terms, privacy, security and possibly regulation; never store bank logins; ask for the price before using it |
| Weather | Bright Sky (DWD open data), later optional providers | Third party | none | CHECK each provider's terms for commercial use (attribution; some require a licence or a contact string) |
| Claude link (SUMMON, orb opens the Claude app) | `Summon.kt` | Uses the public Claude app only | none | No API key is used; a paid feature that calls the Claude API would need Anthropic's commercial terms and per-user keys |

## Rules for threads
- When a task creates something sellable, add one row here (what, where, licence, idea, risk). Mark parts [M] in `docs/design/DESIGN-SYSTEM.md`.
- Keep sellable art as separate files with a clear author line, never mixed into upstream files.
- Never add paid-service keys, ads or trackers without the owner's explicit decision.
