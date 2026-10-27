# Handoff: T14-layer-content

Branch `claude/T14-layer-content` (cut from `claude/integrate-2`). Owner picks: variant C, starred contacts, 7 chips (Wi-Fi, Bluetooth, Flashlight, Auto-rotate, Power saving, Location, Sound).

Changes
- New `app/ui/.../ui/medieval/CommandLayerContent.kt`: layer body (most used apps grid, People-and-switches panel, ribbons, chips). Shown by `CommandOverlay.kt` (3-line edit) when the query is empty; typing swaps to `SearchColumn` as before.
- `MedievalParts.kt` `launchSafely`: now counts a launch (`UsageCounter`, SharedPreferences `medieval_usage`) when the intent names a package. Shields, dock and layer taps use it.
- Most used = own counter first, then the launcher's own `FavoritesService.getFavorites(app, FrequentlyUsed)` ranking (launches via search results); max 8, installed only.
- Contacts: direct `ContactsContract.Contacts` query, STARRED = 1, newest-contacted first, max 5; needs READ_CONTACTS (already declared; section hides without it). Tap = ACTION_VIEW contact card.
- Manifest (upstream file, own commit): `WRITE_SETTINGS` added so the Auto-rotate chip can ask for "Modify system settings".
- Chips: Flashlight `CameraManager.setTorchMode` + torch callback; Sound `AudioManager.ringerMode` Normal<->Vibrate (+Silent only if `isNotificationPolicyAccessGranted`); Auto-rotate `Settings.System.ACCELEROMETER_ROTATION` after `canWrite`; Wi-Fi `Settings.Panel.ACTION_INTERNET_CONNECTIVITY`, Bluetooth `ACTION_BLUETOOTH_SETTINGS`, Power saving `ACTION_BATTERY_SAVER_SETTINGS`, Location `ACTION_LOCATION_SOURCE_SETTINGS`. State reads: WifiManager.isWifiEnabled, Settings.Global bluetooth_on, PowerManager.isPowerSaveMode, LocationManager.isLocationEnabled. Errors logged with tag `medieval` plus a short toast.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL.

NOT verified
- Everything on the phone. api-scout was not run: all API claims above are from Android documentation knowledge and are UNVERIFIED on the S20 FE (torch without CAMERA permission; setRingerMode Normal/Vibrate without policy access, Samsung may differ; `ACTION_INTERNET_CONNECTIVITY` panel; Wi-Fi state read without location permission). Add AF entries after the phone test.
- Layout on a real screen with the keyboard open (the content scrolls; bottom padding = keyboard height).
- Package visibility for `getLaunchIntentForPackage` of arbitrary packages (Shields already rely on it).
- Launcher-ranked apps that are in work profile or private space: only package name is used.
