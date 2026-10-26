# Bug: weather never shows (T4b-fix build, owner phone test 2026-10-26)

Owner followed the setup steps: only the German provider (Bright Sky, DWD) is available in the provider list (MET Norway not offered), picked it, turned location on. The weather spot under the clock STILL says "Tap to set up weather" (forecast stays null). So no temperature, no word, no scene.
Unknown: whether location permission was granted and Android location services are on; whether a forecast request is ever made (does our header collect the same flow Kvaesitso's own weather widget uses?); whether Bright Sky returns data for the owner's location (owner is in a US-based timezone, PDT, Galaxy S20 FE SM-G781U); whether the weather repository only fetches when a weather widget is enabled in widget settings.
