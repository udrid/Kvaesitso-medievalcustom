# S3 research notes: wallpaper, lock screen, icon pack, keyboard (2026-10-27)

Certainty: VERIFIED = read in this repo's code or an official page in this session. likely = consistent across sources, not confirmed on the S20 FE. UNVERIFIED = must be checked on the phone.

## (a) Lock screen and always-on display (One UI 5, S20 FE)
| Fact | Certainty | Source |
|---|---|---|
| Stock settings: lock-screen wallpaper (Gallery image), clock style, clock size/colour, hide clock, notification style; AOD: display mode (tap / schedule / always), clock style and an image option. Path: Settings > Lock screen and Settings > Lock screen > Always On Display. Menu names differ slightly by firmware | likely | [Samsung US community](https://us.community.samsung.com/t5/Discussions/Lock-Screen-Clock/m-p/2365069), [nextpit](https://nextpit.com/how-to-customize-samsung-galaxy-lock-screen) |
| Stock One UI 5 has no free-placement lock-screen widgets or per-screen clock styling; that is what Good Lock adds | likely (not confirmed for 5.0/5.1 on this model) | [SamMobile One UI 5.1 lock screen](https://www.sammyfans.com/2023/03/24/samsung-one-ui-5-1-lock-screen-customization/amp/) |
| One UI 5 support was rolled into Good Lock modules in Oct 2022 (NavStar, ClockFace, Home Up, Keys Cafe...) | likely | [SamMobile](https://www.sammobile.com/news/almost-a-dozen-more-good-lock-modules-now-support-one-ui-5-0/) |
| Modules: Theme Park (build a theme: icons, colours, keyboard), LockStar (lock screen / AOD layout), ClockFace (clock styles, AOD widgets), QuickStar (quick panel and status bar), NavStar (navigation bar) | likely | Good Lock descriptions in the above articles |
| Good Lock is region-gated. The US was on Samsung's country list in 2022; a 2024 report says it stays region-locked; an April 2025 report says the redesigned app goes global via Play Store. Not confirmed for a US unlocked/carrier S20 FE | UNVERIFIED: owner checks Galaxy Store | [Sammyfans](https://www.sammyfans.com/2022/12/10/not-all-countries-eligible-for-samsung-good-lock-app-find-yours/amp/), [SamMobile](https://www.sammobile.com/news/bye-bye-exclusivity-good-lock-app-available-more-markets/), [Android Authority](https://androidauthority.com/samsung-good-lock-play-store-3544482) |
| Newer LockStar/ClockFace builds target newer One UI; the S20 FE (One UI 5.x) may only get older module versions | UNVERIFIED | [Samsung community](https://r2.community.samsung.com/t5/Galaxy-S/Any-update-on-Lockstar-for-One-UI-7/m-p/18402595) |

Plan consequence: the assets work with NO Good Lock (wallpaper + lock + AOD image set by hand in Settings). Good Lock is an optional extra for clock look and AOD layout.

## (b) How Kvaesitso loads icon packs (VERIFIED in this repo)
Code: `services/icons/.../loaders/AppFilterIconPackInstaller.kt`, `IconPackManager.kt`.
- Detection: an installed app with an activity answering ANY of `org.adw.ActivityStarter.THEMES`, `org.adw.launcher.THEMES`, `com.novalauncher.THEME`, `app.lawnchair.icons.THEMED_ICON`. So the standard ADW/Nova format works, no Kvaesitso-specific code needed.
- `appfilter.xml` read from `res/xml/appfilter.xml`, else `res/raw/`, else `assets/`. Root `<resources>`. Tags understood: `<item component="ComponentInfo{pkg/pkg.Activity}" drawable="name"/>`, `<calendar component=... prefix="cal_"/>` (drawables cal_1 to cal_31), `<dynamic-clock .../>`, `<iconback img1=.../>`, `<iconupon>`, `<iconmask>`, `<scale factor="0.9"/>`.
- `res/xml/drawable.xml` (`<item drawable="name"/>`) lists icons for the manual icon picker (VERIFIED parsed).
- Drawables are resolved by name from `res/drawable*`; adaptive icons (`<adaptive-icon>` in `drawable-anydpi-v26`) are supported (`AdaptiveIconDrawableCompat`); plain PNG/vector also works.
- Unmapped apps: if the pack has `iconback`/`iconupon`/`iconmask`, the launcher draws the app's own icon scaled into the back (one back picked at random, the mask cut out, the "upon" laid over). VERIFIED in `generateIcon`. It does NOT draw a letter. So a standard pack cannot make the "gold shield with first letter" for unmapped apps; it can only wrap the real icon in a gold shield back (colour kept).
- Pack sources also: Lawnchair docs [icon packs](https://docs.lawnchair.app/developers/concepts/icon-packs) (manifest actions, file locations; same as code).

**Letter-shield fallback, options** (owner picks): (1) pack-only: iconback = gold shield plate + iconmask shield, the app's real icon shown small inside (colour kept, covers every app); (2) pack gets 26 letter shields A to Z plus an ownership list: not usable for unmapped apps because appfilter maps components, not letters; (3) a small launcher change (new file) that draws the letter shield for apps without a pack icon, as Home already does. 3 is the true match to Home but is launcher code (a later task); 1 is free.

**Coverage**: top apps first. Recommendation: about 120 mapped components for the Samsung/Google stock apps plus the owner's installed apps (X, Discord, YouTube, Spotify, WhatsApp, Gmail, Chrome, Maps, Photos, Camera, Clock, Calendar, Settings, Messages, Phone, Contacts, Samsung apps...), the rest via the fallback. Component names must be read from the phone (UNVERIFIED until the owner exports the installed app list; Part 2 task: one-line steps to list package names).

## (c) Building the icon-pack APK without Android Studio
- Repo GitHub Actions already runs Gradle 17 with `setup-java@v4` (VERIFIED: `.github/workflows/build-medieval.yml`).
- Recommend a **separate small Gradle module in this repo's workspace, but its own workflow and own licence folder**: simplest (reuses the same Gradle wrapper and workflow style, no new repo, no secrets) and the art stays separate files (monetisation rule). A separate repo is better only at selling time (own licence, own signing key); moving a module out later is a copy. Keep the module off the launcher's `settings.gradle.kts` includes if a clean split is wanted: UNVERIFIED which is simpler; decide with the owner.
- Needs: `AndroidManifest.xml` (one launcher-less activity with the ADW intent filters), `res/xml/appfilter.xml`, `res/xml/drawable.xml`, drawables. No code beyond a stub activity.
- Play Store basics: one-time US$25 developer registration, identity check ([Google](https://support.google.com/googleplay/android-developer/answer/14659200)); sold under paid app or in-app purchase; the licence of fonts (OFL) allows embedding glyphs converted to paths. Do not use the game's logos or marks. Not legal advice; CHECK trademark of the name. Installing it by hand needs no store.

## (d) SwiftKey theme
- SwiftKey (Android) Themes > Custom tab: pick a photo (JPG/PNG), brightness (default about 45%), pop-up colour from a preset list, key background from greys/transparent, key borders on/off, symbols on/off. No custom fonts, no free colour picker, no key-shape editing. Sources: [Microsoft support: photo themes](https://support.microsoft.com/en-us/topic/how-to-use-photo-themes-in-microsoft-swiftkey-keyboard-839761ea-04dd-4779-bba6-0c21dc5a01dc), [Android Police](https://www.androidpolice.com/2017/10/29/switkey-betas-new-photo-themes-lets-use-images-create-custom-keyboards/). 2017-era pages: menu names UNVERIFIED on the current app.
- So we deliver: one charcoal-vellum PNG (the keyboard sits on top of it), plus the colour choices to tap (darkest key grey, gold-ish or nearest pop-up colour, borders on). Key shape and gold letters are not achievable: SwiftKey letters are white/grey by its own light/dark rule (UNVERIFIED). Samsung's own keyboard + Good Lock Keys Cafe/Theme Park can colour more but is a different keyboard.
- Image size: wide-short; recommend 1080x1000 px, safe for crop (UNVERIFIED which size SwiftKey crops to).

## (e) Wallpaper facts for 1080x2400 (S20 FE)
- Screen 1080x2400 (S20 FE, 6.5", 20:9; VERIFIED spec), hole-punch camera top centre (the launcher's pill is at the same place), gesture/3-button bar at the bottom. Keep faces/text out of top 120 px and bottom 160 px.
- Samsung lock screen shows the clock about top third and notifications over the middle: keep the middle quiet, ornament only in the lower third and corners.
- The launcher Home is drawn on its own charcoal ground (C5 grain and vignette). Recommendation: **Home keeps its own background**, a launcher wallpaper is only seen through the launcher's transparent areas, so a busy wallpaper would fight the grain. Use a near-plain charcoal wallpaper for Home, and the richer artwork for the lock screen (separate image, set in Settings). Samsung allows different images for home and lock (likely; "Wallpaper and style" shows both).
- Dark mode (Settings > Display > Dark): VERIFIED standard setting; some apps follow it.

## Not verified (to say in the final report)
Good Lock availability and module versions on this phone; menu names on One UI 5.x; SwiftKey current menu and crop size; installed app list and component names; that the launcher picks the pack on the phone (code read only, no run).

## Built 2026-10-27 (Part 2)
What was built and the owner picks: tasks/S3-assets/picks.md and handoff.md. App mapping and plate fallback are UNVERIFIED on the phone.
