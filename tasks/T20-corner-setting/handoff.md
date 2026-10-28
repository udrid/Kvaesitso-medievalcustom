# Handoff: T20-corner-setting (built 2026-10-28)
Built: setting "Corner style" (Sharp default / Round) on Settings > Appearance. Two-value choice for buttons and chips (the owner said "toggle for both round and square").
- NEW `medieval/MedievalCorners.kt`: `CornerStyle`, `Corners.style` (Compose state), `LocalCornerStyle`, SharedPreferences `medieval_home` key `corner_style`, `MedievalCornerPreference`. Nothing cached in top-level vals; shapes are computed per read.
- NEW `medieval/MedievalButtons.kt`: `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton` with Material's signature, default shape from `LocalCornerStyle`.
- Upstream files (separate commit): 47 files only changed the import of those four buttons to `de.mm20.launcher2.ui.medieval.*` (same trick as AlertDialog); 3 files with `material3.*` got one explicit TextButton import. `ConfigureWidgetSheet.kt` left on Material (its FilledTonalButton uses `shapes = ButtonDefaults.shapes()`, stays a pill). Libs login activities untouched.
- `LauncherTheme` loads the choice and provides `LocalCornerStyle`; Home also reads it with the palette. `AppearanceSettingsScreen`: Theme import/export and Colour source hidden; Corner style added.
- Typing-layer `SwitchChip` follows the style (Round = 16 dp pill, as before). Summon button and command bar were already square.
Changed by the setting: dialog and sheet buttons, all settings and widget buttons using the four above, typing-layer chips. NOT changed: panels, cards, frames, ribbons, wax seal (round always), Material chips (FilterChip/AssistChip/InputChip, already sharp via the theme's shapes, stay sharp in both modes), small round dots/badges/orb/camera pill/coin.
Seen: `docs/design/shots/T20/` (dialog and typing layer in Gold, Ember, Royal, Sharp then Round on the same open screen; Appearance). Test `T20Shots.kt`. Popup list of Corner style is a window, not in the picture.
NOT verified: phone. Material chips do not become round in Round mode (decision for the owner if wanted).
Compile green; `:app:ui:testDebugUnitTest --tests "*T20Shots*"` green.
