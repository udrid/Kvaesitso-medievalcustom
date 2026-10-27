# Handoff: T16-polish
Branch `claude/T16-polish` (from `claude/integrate-3`). All edits in `app/ui/.../ui/medieval/`, no upstream files.

1. Divider (`PlanetaryDays.kt`): kept the C5 look, shortened only the RIGHT end. The weather box is x 272..346 in the header. The rule now ends at `RuleEnd` = 266 (6 units clear of the box) and a mirrored ornament is drawn only if its right edge is <= 266. Left side and centre unchanged. I did not lift the scene: from the owner's screenshot the scene sits about 8-10 units lower than my arithmetic (line boxes), its bottom at about the divider level, so only a horizontal fix is safe regardless of the exact vertical position.
   Gap from the scene box (x 272) to the last right-hand ornament / rule end, by hand from the path data (Sol 12 dots, etc.):
   | Day | Right ornaments kept | Last edge | Gap to scene |
   |---|---|---|---|
   | Sol | 2 dots | 256.6 | 15.4 (rule 6) |
   | Luna | 1 moon | 238.5 | 33.5 (rule 6) |
   | Mars | 3 chevrons | 253.7 | 18.3 (rule 6) |
   | Mercury | 4 feathers | 263.6 | 8.4 (rule 6) |
   | Jupiter | 1 bolt | 246.5 | 25.5 (rule 6) |
   | Venus | 2 loops | 263.6 | 8.4 (rule 6) |
   | Saturn | 3 diamonds | 259.2 | 12.8 (rule 6) |
   The rule line itself ends at 266 on every day, 6 clear. Right side is now sparser than the left (by design).
2. Gap: `BodyLift = 8` (`HeaderSection.kt`): body top 150 -> 142 (stage). Dispatches height 90 -> 97.4 body units (+8 stage), dock rule and dock row +7.4 body units (= +8 stage), so the dock stays at the same stage position; header, pill and the other sections are untouched. Body box height raised by the same amount.
3. Contacts (`CommandLayerContent.kt`): `CommandLayerVM` got the permissions manager and the Contacts flow. Not granted: line "Tap to allow contacts" (italic, tap = `requestPermission(Contacts)`, same route as Ledger/Calendar). Granted and none starred: "Star contacts in your Contacts app to see them here". The ribbon reads "People and switches" once the state is known. Layer spacing 26 -> 20 so more fits above the keyboard; the layer already scrolled and most used apps are first.
4. Stars (`OrbGlass.kt`): grains 10 -> 7, rate 1.3 -> 0.9 (period ~4.8 -> ~7 s), floor 0.35 -> 0.6, gain 0.6 -> 0.4: lit about 28 % -> 21 % of the time per grain, peak 0.6 -> 0.4. Turn speed and fps untouched.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL.
NOT verified (phone only): the real gaps (computed from path data, not seen), how the shortened divider looks, that the dock is exactly unmoved (arithmetic), that the Contacts permission flow updates the panel at once after Allow, whether the owner's contacts are starred at all, the new twinkle level.
