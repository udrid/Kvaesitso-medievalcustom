# Task brief: T4c-weather

Goal: The weather line and scene on Home show real weather for the owner's location. Branch `claude/T4c-weather` from `claude/T4b-fix`. Read `tasks/T4b-fix/handoff.md`, `tasks/T4-header/handoff.md` and `tasks/T4c-weather/bugs.md`.

Do:
1. Diagnose from the code why the forecast stays null. Read `WeatherRepository` and how Kvaesitso's own weather widget gets forecasts (flow, lifecycle, any "enabled" or "has location" gating) and compare with how `HeaderSection`/`MedievalHomeVM` get them. Use `api-scout` for Bright Sky and the Kvaesitso weather provider contract; Bright Sky worldwide coverage for a US location is UNVERIFIED, check it. Check what other providers exist in this repo (`plugins`, OpenWeatherMap, Breezy, MET Norway needs?) and why only the German one is offered.
2. Fix the cause in our code if it is ours. If the provider cannot serve a US location, report it and propose options as multiple choice for the owner (for example A) add the MET Norway provider if the repo has it, B) a different free provider that needs no account, C) a plugin). Do not choose; ask the owner. A provider that needs an account or key is a cost/key question: ask first.
3. Add visible, honest states in the weather spot instead of the one hint: "Allow location" when permission is missing, "Fetching weather…" while loading, "Weather unavailable" when the provider fails (log with tag medieval, never the owner's coordinates). Keep the "Tap to set up weather" only when no provider is selected. Wording is delegated to you, keep it short and in the style.
4. If a manual location (typing a city) exists in Kvaesitso's weather settings, write plain steps for it in the testlist as the fallback.

Rules: commit locally; do NOT push or start a build without the owner's approval (CLAUDE.md); compile with `./gradlew :app:ui:compileDebugKotlin` (JAVA_HOME = Android Studio jbr). Commit trailer `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`. Done when: gate items 2-4, `testlist.md` (plain steps; should-NOT-happen step; restart step), `handoff.md`, lessons (0-3 lines), STATUS row, then report READY TO PUSH with a plain-words summary. Two failed fixes: stop, write root cause in bugs.md.
Out of scope: any other layout or design changes, Treasury.
