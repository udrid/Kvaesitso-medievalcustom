# Task brief: T0-build

Goal: The unmodified Kvaesitso fork builds in GitHub Actions on every push to `claude/*` branches and gives a signed APK that the owner installs on the phone; the launcher opens.
Files in scope:
- NEW `.github/workflows/build-medieval.yml` (copy of `build-nightly.yml`, triggered on push to `claude/**` + manual). Do not edit upstream workflows.
- GitHub repo secrets `KEYSTORE`, `KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`: the fork has none, so the upstream build fails at signing. Generate one keystore with `keytool`, set the secrets with `gh secret set`, keep it in a backup outside the repo, and tell the owner where. Never ask the owner for a password. Use ONE key for the life of the project: a new key forces an uninstall, which wipes launcher settings.
- `tasks/T0-build/testlist.md`, `handoff.md`.
Done when: the gate passes, the Actions run is green, the APK artifact downloads, and the owner reports that the launcher installs and opens and can be set as Home, with Smart Launcher still installed.
Out of scope: any look, renaming the app to "Deliverance" (that comes in T1 or later), changing the applicationId.
Open questions (ask the owner, multiple choice): none.
Notes: the owner downloads the APK from the Actions run page (a zip) on the phone or a PC. The testlist must explain "Install unknown apps" for the browser/Files app, in plain steps.
