# Deliverance Icons (icon pack)

A standard Android icon pack (ADW / Nova / Lawnchair format) that Deliverance (Kvaesitso) reads. Three variants install side by side:
heater (shield plate, like the Home shields), seal (round plate) and bare (emblem only, no plate).

- Build: `./gradlew -p iconpack :pack:assembleHeaterDebug :pack:assembleSealDebug :pack:assembleBareDebug` (JDK 17, Android SDK). In GitHub: Actions > "Build icon pack APKs" > Run workflow (manual only).
- Do not edit `res/xml/appfilter.xml`, `res/xml/drawable.xml` or the PNGs by hand: they are generated from `assets/src/` (see `assets/README.md`).
- Art and licence: see `LICENSE-ART.md`. The Gradle and Java files are small helper code.
- This folder is its own Gradle build on purpose, so it can move to its own repository when it is sold.
