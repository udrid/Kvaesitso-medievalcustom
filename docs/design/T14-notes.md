# T14 notes: data for the typing layer (checked in code, not on the phone)

## Most used apps: yes, no new permission
- Kvaesitso counts launches itself. `SavedSearchableEntity.launchCount` + `weight` (decays, favours recent use) live in the launcher's own database; `SearchableDao.get(frequentlyUsed = true, ...)` returns items with `launchCount > 0`, ordered `pinPosition DESC, weight DESC, launchCount DESC`. `FavoritesService.getFavorites(includeTypes = ["app"], minPinnedLevel = FrequentlyUsed, maxPinnedLevel = FrequentlyUsed, limit = 8)` is the ready-made call (it is what the upstream "frequently used" favourites use).
- Counting happens in `FavoritesService.reportLaunch`, called from `SearchableItemVM` (results, favourites) and `SearchVM` (Enter on best match). **Gap to check:** launches from this fork's own Home pieces (shields, app picker, dock) may not call `reportLaunch`; if so, apps launched only from there never rank. Part 2 would add `reportLaunch` to those taps (small, fork-only code). Android's own UsageStats would need the special "usage access" permission: not wanted.
- Empty on a fresh install until apps are launched through the launcher. Fallback: show nothing for that section (no fake entries).
- Icons: T7 `ShieldEmblems.forPackage` gives a line emblem for known apps, otherwise the letter shield.

## Recent contacts: needs Contacts permission (already declared)
- `READ_CONTACTS` is in the manifest; `ContactRepository` checks `PermissionGroup.Contacts` and returns nothing without it. The launcher asks through its settings (search settings > contacts). The owner may already have granted it for search; if not, the section would show a quiet "Allow contacts" line.
- "Recent" is not available from the current provider (it only does name search). Two options with no extra permission beyond READ_CONTACTS: **starred** (`Contacts.STARRED = 1`, owner's favourites) or `Contacts.LAST_TIME_CONTACTED` / `TIMES_CONTACTED`. Caveat: Android 10+ stopped updating those two columns for privacy, so on Android 13 they are mostly stale. Real "recently called" needs `READ_CALL_LOG` (a sensitive permission Google Play restricts; fine for a sideloaded app but a new prompt). **Recommendation:** starred contacts first; optionally recently launched contacts (our own `launchCount` for contacts tapped through search). Tap = open the contact card (or dial on long-press, uses existing CALL_PHONE).
- Photos: `Contacts.PHOTO_THUMBNAIL_URI`, else initials in a gold ring.

## Quick settings: open, do not toggle
- Without special permission an app on Android 13 cannot flip these. Wi-Fi/Bluetooth toggling is blocked for third-party apps (Android 10+ / 13); Do Not Disturb needs "Do Not Disturb access"; brightness needs WRITE_SETTINGS. Plan: each chip opens the system's own panel/page, no promises of a toggle:
  - Wi-Fi: `Settings.Panel.ACTION_INTERNET_CONNECTIVITY` (small sheet), Bluetooth: `Settings.ACTION_BLUETOOTH_SETTINGS`, Do Not Disturb: `Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS`, Airplane: `Settings.ACTION_AIRPLANE_MODE_SETTINGS`, Display: `Settings.ACTION_DISPLAY_SETTINGS`, Sound: `Settings.ACTION_SOUND_SETTINGS`.
  - **Flashlight is the exception:** `CameraManager.setTorchMode()` works with no permission (CAMERA not required for torch). Real toggle, filled gold when on. UNVERIFIED on this phone: probe once.
- The chips could mirror state (Wi-Fi on/off) since the pill (T5) already reads it; optional.

## Extra ideas (at most two, both quiet)
1. **Ask the Oracle** (3 starter prompts, shown in variant A): tap sends the prompt through the existing SUMMON route (share + clipboard). Reason: it makes the SUMMON button meaningful before typing, and costs no permission. Owner would write/approve the prompts.
2. **Recent searches** (last 3 typed queries as small italic lines under the bar): reason: fastest way back to what you looked up; stored locally only. Slightly busier, so not in the mockups.
Not proposed: today's next event (Home already has Ledger), news/weather (already on Home).

## Recommendation
Variant **C** (apps get 2 rows of 4; people and switches share one framed panel, matching the Home panels): it is the least busy, the apps stay the star, and it fits above a half-height keyboard. Contacts = starred. Chips: Wi-Fi, Bluetooth, Do Not Disturb, Flashlight (4). Optional extras: Ask the Oracle prompts only. Empty sections hide themselves.
