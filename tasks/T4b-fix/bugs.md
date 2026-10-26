# Bugs: found by the owner on the phone with the T4-header build (2026-10-26, Galaxy S20 FE, 1080x2400, 3-button nav bar)

Evidence: owner screenshot (Monday). Observed:
1. Layout is off-center / not matching the preview:
   - The four pennants do not span the same width as the panels: they start near the left edge of the Treasury panel but the last one ends well short of its right edge (the row looks shifted left).
   - The dock (Herald, Portrait, Road, Smithy) is not evenly spread and is shifted; its labels sit ON TOP of Android's navigation buttons (the three-button bar overlaps the labels). The 8% scaled body is taller than the free screen height.
   - Orb ring looks good; left shields vs right shields roughly symmetric but please measure all sections' horizontal centre against the panels.
   - The header medallion and the right-hand time are fine; the page may be 1-2 dp off.
2. Weather: the weather line shows "[weather]" and there is no landscape picture. Probably no provider configured (T1 handoff says the weather line shows [weather] until a provider and location are set in the launcher settings), but the owner has NO obvious way to open the launcher's settings from this Home screen, and "Smithy" opens Android settings.
Not a bug: "Screenshot saved" in Dispatches works.

Root cause: not yet known. Diagnose by reading MedievalHomeComponent.kt (stage scale = min(width/390, height/844), BodyScale 1.08, transform origin), the section coordinates, and window insets (status and navigation bars).
