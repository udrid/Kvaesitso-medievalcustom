# Seeing the UI on this PC (R1, 2026-10-27)

## The Android emulator does NOT run here
- `emulator -accel-check` -> "Android Emulator requires an Intel/AMD processor with virtualization extension support. (Virtualization extension is not supported)".
- Why: this PC is a cloud VM (AMD EPYC 9354, `HypervisorPresent = True`, `SecondLevelAddressTranslationExtensions = False`). The host does not pass nested virtualization (VT-x/AMD-V + SLAT) through, so neither WHPX nor AEHD can work. This is a property of the cloud machine, not a setting we can turn on.
- Software mode (`-accel off`) with the installed `android-37.0` x86_64 image starts, warns "x86_64 emulation may not work without hardware acceleration!" and crashes (exit code 139). There is no arm64 image for software mode in the SDK, and it would be far too slow anyway.
- So: no emulator, no APK install, no `adb screencap` on this PC.

## What works: Robolectric screenshot tests (Compose rendered to PNG on the JVM)
Test-only setup (not in the APK): `app/ui/build.gradle.kts` has `testOptions.unitTests.isIncludeAndroidResources = true` and `testImplementation` of junit, Robolectric 4.17, mockk 1.14.11, compose `ui-test-junit4` and `ui-test-manifest`. The test is `app/ui/src/test/java/de/mm20/launcher2/ui/medieval/MedievalShots.kt`.

Run (Git Bash, from the worktree root; first run downloads ~300 MB, later runs ~1 min):
```
cp /c/Users/Shadow/Kvaesitso-medievalcustom/local.properties .   # once per fresh worktree
export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"
./gradlew :app:ui:testDebugUnitTest --tests "*MedievalShots*" -i | grep "medieval shot"
```
Pictures land in `docs/design/shots/` (open them with the Read tool):
- `home-portrait.png` (411 x 914 dp at 420 dpi = S20 FE), `home-landscape-camera-left.png`, `home-landscape-camera-right.png`, `home-landscape-typing.png` (typing layer open), `home-small-640x360-landscape.png`, `home-small-360x640-portrait.png`.
- Blue band = where the phone's navigation bar sits; red dot = the camera hole. Nothing of ours may be under either.

How it works: `@GraphicsMode(NATIVE)` makes Robolectric draw with the real Android graphics stack; `@Config(qualifiers = "w914dp-h411dp-land-420dpi")` sets the screen; navigation-bar and cutout insets are dispatched by hand; view models are replaced by mockk fakes through a custom `ViewModelStoreOwner` (no Koin, no real accounts, fake calendar/weather data).

Limits (what the pictures do NOT show):
- Anything that reads the real phone: the camera pill reads the display cutout from the window, not from our fake insets, so the pill is drawn at its fallback place. Battery, storage, Wi-Fi, notifications, installed apps (shields say EMPTY, "Most used" is empty).
- Samsung behaviour: One UI insets, rotation animation, status bar handling, fonts the user scaled, real speed and battery.
- Animations are shown frozen at the time `mainClock.advanceTimeBy` reaches.
So a picture here catches layout and drawing bugs (overlaps, clipping, wrong position), not phone-specific ones.

## Adding a picture for a new screen
Copy one `@Test` in `MedievalShots.kt`, set the qualifiers, and render the composable you changed inside `rule.setContent { ... }`. If it needs a view model, add a fake in `FakeOwner` (stub only the fields the screen reads; `mockk(relaxed = true)` for the rest). Upstream screens that need Koin repositories need the same trick or a small fake module.

## Proposed line for CLAUDE.md "Definition of done" (coordinator decides)
"Before READY TO PUSH, render the changed screens with the screenshot test (`ops/emulator.md`), look at the PNGs (portrait and, if touched, both landscape turns), and attach them under `docs/design/shots/`."
