# Handoff: R1-review stage 1

What changed:
- Emulator probed: cannot run on this PC (cloud VM, no nested virtualization; software mode crashes). Replaced by Robolectric screenshot tests: `app/ui/src/test/.../medieval/MedievalShots.kt` + test-only deps in `app/ui/build.gradle.kts` (own commit). Steps in `ops/emulator.md`. Pictures in `docs/design/shots/` (portrait, landscape camera left/right, typing layer, 640x360 and 360x640).
- Landscape fix (medieval/ only): `overflowTopStart()` before every oversized `requiredSize` (sections in `Place()`, Oracle box, stage box, Pennants) so Compose no longer centres them (bugs 1-4); landscape free area clamped to the visible part of Home's box, measured once per orientation, with a `Stage:` log line (rest of bug 1, phone-side, unconfirmed); smaller weather scene and row 4 units higher (bug 5); Ledger rows limited to the column width in landscape (ellipsis). Causes in `bugs.md`.
- S2 review: `review-s2.md` (8 findings, two HIGH: sheet content width, dialog not scrolling).
How it was checked: `./gradlew :app:ui:testDebugUnitTest --tests "*MedievalShots*"` builds (includes compiling app:ui) and the PNGs were looked at: landscape both ways matches layout A with no overlaps; portrait at S20 FE size unchanged; small screens now fit.
NOT verified:
- Anything on the S20 FE. The renders use fake insets, fake data, frozen animation, no Samsung.
- Why the phone drew the S1 stage ~9 dp too high: not reproduced on the PC. The clamp is defensive; the `Stage:` log line (tag `medieval`) shows the real numbers.
- The camera pill (it reads the real cutout, which the test cannot fake); owner said it looks right.
- The rotate glide, taps in landscape, the shield picker, the S2 screens (not rendered).
Next stage needs: owner photos of both landscape turns; S2 fixes routed by the coordinator; screenshot tests for S2's screens before its next build.
