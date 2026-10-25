# Medieval launcher: rules for every Claude session

Loaded automatically in every session started in the repo root. Keep it under 120 lines.
Project: a custom Android launcher (fork of Kvaesitso, GPL-3.0) for the owner's Galaxy S20 FE (SM-G781U, Android 13). Look: Kingdom Come: Deliverance, dark edition. Set up 2026-10-25.

## Project
- Owner decides every look and feature. Approved design: **C5** (`docs/design/C5.png`, source `docs/design/C5.html`, written spec `docs/design/SPEC-C5.md`). Do not redesign it. Only C5 is in the repo; the other drafts were rejected.
- Phone is stock Samsung, locked bootloader: apps and launchers only, no custom OS.
- Build: GitHub Actions compiles the APK (`.github/workflows/`). Android Studio and local Gradle may not exist. The owner installs the APK on the phone. The owner holds no Claude API key and wants none yet.
- Keep Smart Launcher installed as a fallback. Never ask for passwords, bank logins or card numbers. No personalized financial advice.

## Where things are
| What | Where |
|---|---|
| Source of truth for code | `app/`, `core/`, `data/`, `services/`, `plugins/` (upstream layout) |
| Design | `docs/design/` |
| Task status and open questions | `ops/STATUS.md` |
| Owner-approved decisions | `ops/decisions.md` |
| Verified Android and Kvaesitso facts | `ops/api-facts.md` |
| Checks | `ops/gate.md` |
| Templates | `ops/templates/` (brief, handoff, bugs) |
| Old notes (don't read unless asked) | `ops/archive/` |

## Read these, nothing else by default
1. This file. 2. Your row in `ops/STATUS.md`. 3. `tasks/<id>/brief.md` (+ `handoff.md`, `bugs.md` if present).
Need more? Search the code (Grep), then open only the files that match. Never read whole folders.

## Owner rules
- Ask before ANY design decision: look, name, layout, wording, a new feature. Give choices as multiple choice. Only decisions the owner delegated in `ops/decisions.md` may be made by Claude.
- Whenever the owner must do something (install the APK, grant a permission, test, answer), write numbered steps in plain words: where to be on the phone, what to tap, what they should see, what to report. Assume no technical background. Never say only a step code. At most 3 do-lines per step.
- Say what was NOT verified. The workspace can't run the app; only the owner's phone can.
- Treasury (finance panel): placeholders only, no invented numbers. Manual entry first. Bank links need a backend, keys and cost: ask first.

## Code rules (Kotlin / Android)
- Use `ops/api-facts.md`; don't re-derive facts. For an unverified call, ask the `api-scout` subagent. If still UNVERIFIED, write a tiny probe and test it on the phone, then add an AF entry.
- No empty `catch`. Log every caught error with a tag (`Log.e("medieval", ...)`).
- No "try A, catch, try B" guessing. Probe once and keep the call that works.
- Keep upstream code changes small and in separate commits so upstream updates stay mergeable. New code goes in new files or modules where possible.
- Respect GPL-3.0: keep headers and `LICENSE.txt`.
- Commit messages end with: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## Definition of done (each stage)
1. `ops/gate.md` checks pass; the GitHub Actions build for the branch is green.
2. Owner checks for the phone are written in `tasks/<id>/testlist.md` (see Owner rules): the visible result first, at least 1 "should NOT happen" step, and a restart-the-launcher step if the feature saves data.
3. `tasks/<id>/handoff.md` written from the template.
4. 0-3 lines in `ops/lessons-inbox.md` (surprises only).
5. STATUS row updated. Say FINISHED and stop.

## Threads and the coordinator
- One thread = one stage of one task, started in the repo root. Never resumed after FINISHED; the coordinator archives it.
- No `/loop`, scheduled tasks or idle notices in threads. Never message a thread whose STATUS row isn't `active`.
- Independent stages run in parallel on their own branches (`claude/<task-id>`), never the same files.
- Fixes after testing: a NEW thread from brief + handoff + bugs. Two failed fixes of one bug: stop, write the root cause in `bugs.md`, escalate.
- The coordinator starts fresh each work session from `ops/STATUS.md`, updates it before stopping, and doesn't stay open for days. It reads diffs and the build log itself and never trusts an agent's "done".
- Threads propose rule changes as lessons lines. Only the coordinator and the owner change this file, `decisions.md` or `api-facts.md`.

## Models
- Haiku: `api-scout`, `gate-runner`, `test-steps`, `Explore`, weekly lessons pass.
- Sonnet: writing and fixing Kotlin, triage, `reviewer` (medium/high risk only).
- Opus: planning a new feature, or a bug that survived two Sonnet fixes.
