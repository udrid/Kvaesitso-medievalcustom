# Handoff: T0-build stage 1

What changed:
- NEW `.github/workflows/build-medieval.yml`: copy of build-nightly, runs on push to `claude/**` + manual, builds `assembleDefaultNightly`, uploads artifact `medieval-launcher-apk`. Version code = 2026100000 + run number (so each build installs over the last). Upstream workflows untouched.
- One signing keystore generated with keytool (PKCS12, RSA 2048, alias `medieval`, 10000 days). Backup OUTSIDE the repo: `C:\Users\Shadow\medieval-keystore\` (`keystore.jks`, `keystore.b64`, `secrets.txt` with the passwords). Keep a copy somewhere safe (USB / cloud drive). Never commit it.
- `tasks/T0-build/testlist.md` written.

How it was checked: YAML re-read against build-nightly and the `gh-actions` signingConfig in `app/app/build.gradle.kts` (env names match). Nothing compiled locally.

NOT verified:
- The GitHub secrets are NOT set yet: `gh` is not installed here and I had no GitHub login. Required repo secrets: `KEYSTORE` (contents of keystore.b64), `KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD` (values in secrets.txt). Until set, the Actions run fails at signing.
- No Actions run has been seen green; no APK installed on the phone.
- The applicationId of this build is `de.mm20.launcher2.nightly` (nightly suffix), a separate app from any upstream install.

Next stage needs: green run + owner's report from testlist. T1-home stays blocked until then.
