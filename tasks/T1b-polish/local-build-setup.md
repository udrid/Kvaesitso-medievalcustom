# Build on this computer and install over USB (owner steps)

Needs about 10-15 GB free. The computer has about 25 GB free, so this fits but close other big things first.

## Part A: install Android Studio (once)
1. On the computer, open a browser and go to developer.android.com/studio. Click the big download button for Windows and run the installer, accepting the defaults.
   - You should see Android Studio open a setup wizard.
   - Report: any error message.
2. In the wizard choose "Standard" setup and let it download the SDK. Wait until it says it is finished (can take 10-20 minutes).
3. Click "Open" and pick the folder `C:\Users\Shadow\Kvaesitso-medievalcustom`. On the branch `claude/T1b-polish`.
   - You should see "Gradle sync" running at the bottom. The first time it downloads a lot and takes 10-30 minutes.
   - Report: whether it ends with "sync successful" or an error (copy the red text).

## Part B: prepare the phone (once)
1. On the phone open Settings > About phone > Software information.
2. Tap "Build number" 7 times quickly. You should see "Developer mode has been turned on".
3. Go back to Settings > Developer options, turn on "USB debugging".
4. Plug the phone into the computer with the USB cable. On the phone tap "Allow" on the "Allow USB debugging?" question (tick "Always allow").
   - Report: whether the phone's name appears at the top of Android Studio.

## Part C: each time you want to test
1. In Android Studio pick the `app` configuration and the build type `defaultNightly` (Build Variants tab on the left edge), then press the green Run triangle with your phone selected.
2. After 1-3 minutes the app opens on the phone. Deliverance installs over the old one and keeps your picks.

I (Claude) can then build the same thing from the terminal once the SDK exists, for example `./gradlew installDefaultNightly`. Tell me when Part A step 3 is done and I will check it.
