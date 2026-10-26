# Handoff: T5b-fix (bug 1, pill too high)

Cause (best judgement from the two photos, NOT proven): the hole sat about 5-6 dp low in the pill, so the display-cutout rect from `boundingRects` is a bounding box taller than the real hole and its centre is above the hole's centre.
Change (branch `claude/T5b-fix`, only `MedievalPill.kt`, `findHole`): on Android 12+ (API 31) the pill now centres on the bounds of `displayCutout.cutoutPath` (the hole's real outline); if the path is missing it uses the rect as before. The log line (tag `medieval`, "Pill: cutout rect ..., hole shape ...") shows both so a wrong guess can be read from logcat.
Hole circle size is now taken from the real outline too (pill height = max(26 dp, hole + 10 dp), unchanged formula).
Checked: `./gradlew :app:ui:compileDebugKotlin --offline` succeeds. Nothing pushed, no build.
NOT verified: everything on the phone. Unknown whether Samsung returns a non-null `cutoutPath`; if null the pill behaves exactly as before (still too high). Then next step: read the log line, or add a fixed dp nudge as the owner's call.

Addition (owner-approved via coordinator): when the cutout path is not available (API below 31, path null or empty bounds) the hole centre is moved down 5 dp. The 5 dp is a GUESS from the two photos, not measured. With the path, no nudge. The log line (tag `medieval`, "Pill: cutout rect ...") says "using cutout path, no nudge" or "no cutout path, nudge ...px down", so the owner/coordinator can tell which branch ran.
Compile: same command, no errors. Not verified on the phone.
