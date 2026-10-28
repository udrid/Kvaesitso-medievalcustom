# Handoff: S3-assets (Part 2 built)

What changed (all new files, no upstream file touched):
- `assets/`: orb wallpaper (1C), seven AOD day images (2B), SwiftKey picture (6A), icon sheets, sources in `assets/src/` (lib.js, emblems.js, render.html, icons.html, mapping.tsv, build-iconpack.ps1), `assets/README.md`.
- `iconpack/`: standalone Gradle build, flavors heater / seal / bare (43 emblems each, 108 mapped app components, fallback plate for other apps), `LICENSE-ART.md`, README.
- `.github/workflows/build-iconpack.yml`: manual only (workflow_dispatch).
- Part 1: `docs/design/S3-assets.html/.png`, `S3-notes.md`, monetisation rows. Owner picks: `tasks/S3-assets/picks.md`. Owner steps: `testlist.md`.

How it was checked: images and sheets rendered with Edge and looked at. `./gradlew -p iconpack :pack:assembleHeaterDebug :pack:assembleSealDebug :pack:assembleBareDebug` built clean locally (JAVA_HOME = Android Studio jbr, ANDROID_HOME set; NOT --offline, the Kotlin plugin it needs was not cached). aapt2 shows the appfilter and the package name inside the APK.

NOT verified: that the phone and Deliverance accept and show the pack (code read only); that the component names in `mapping.tsv` are right (from memory; an exact match is needed, VERIFIED in IconDao); the fallback plate size (`scale 0.52`); Samsung's AOD image option on One UI 5.x; Good Lock availability; SwiftKey menus and crop; whether the orb sits clear of Samsung's lock-screen notifications. Icons are plain PNG: not adaptive, not Material You themed.

Next stage needs: the owner's testlist answers (apps without an emblem; AOD and SwiftKey findings). Follow-ups to propose: 3C (the launcher draws letter shields for apps the pack lacks: small launcher task reusing Home's letter shield); a phone-wide colour-scheme switch (owner request 2026-10-27, separate task, Home and launcher first with a preview); fix `mapping.tsv` from the owner's list.
