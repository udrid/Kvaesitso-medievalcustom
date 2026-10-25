# Task brief: T1-home

Goal: The launcher's Home screen looks like approved design C5 (`docs/design/C5.png`, `C5.html`, `SPEC-C5.md`), named "Deliverance", with real data where it is simple and placeholders elsewhere.
Branch: `claude/T1-home` (from `claude/T0-build`, which is verified on the phone).

Files in scope:
- NEW code in a new module or new files (theme, fonts, Home composables). Keep edits to upstream files small, each in its own commit.
- Fonts: UnifrakturCook, Cinzel, IM Fell English (all OFL; bundle the .ttf files in `res/font` and keep their licence files).
- App label "Deliverance" only (string resource). Do NOT change the applicationId.
- `tasks/T1-home/testlist.md`, `handoff.md`.

Build in this order (one commit each, push so Actions builds):
1. Theme + fonts + grain/vignette; app label.
2. Header (drop cap, date, big clock, weather line if Kvaesitso already has a weather source; else placeholder).
3. Status line (Wi-Fi call, Bluetooth, Hushed, voicemail): real values only where a simple API exists, else placeholder.
4. Treasury panel: PLACEHOLDERS ONLY, no invented numbers (rule in CLAUDE.md; SimpleFIN is T2).
5. Four pennants: Vigor = battery, Stores = storage, Wits = memory, Heat = battery temperature. Real values.
6. Orb: tap opens the Claude app (empty). Ring text per `ops/decisions.md`. Label "The Oracle".
7. Four heater shields around the orb. Badges are sample-free: hide the badge until notification access is built (T3).
8. Today's Ledger: next two calendar events (use Kvaesitso's calendar search/provider; READ_CALENDAR permission flow).
9. Command bar "Command the Oracle..." = the launcher search (apps, contacts, calendar, settings, web). SUMMON hands text to the Claude app only if AF entry says it works; otherwise leave SUMMON out of the build and report it.
10. Dock: wax seals Herald (Phone), Portrait (Camera), Road (Web), Smithy (Settings), opening the default apps.

Done when: gate passes, Actions run green for the branch, testlist and handoff written, and the owner has the APK steps. Owner verification happens after FINISHED in a new session.
Out of scope: camera pill (own overlay service, later task), Dispatches / notification badges (T3, needs notification access), Treasury data (T2), any redesign.

Open questions (ask the owner, multiple choice, before step 7):
- Which apps do the four shields open? C5 shows Twitter, Discord, Videos, Messages. Options: A) exactly those if installed (X, Discord, YouTube, Samsung Messages), B) owner picks apps from a list in settings. Ask the owner; default nothing until answered.
Notes: use `ops/api-facts.md`; use `api-scout` for any unverified Android/Kvaesitso call. State what was NOT verified; the workspace cannot run the app.
