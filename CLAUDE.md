# Handoff: Medieval custom Android launcher (coordinator + agents)

This file is the project's `CLAUDE.md`.

> NOTE FOR THE USER: I could not find your Minecraft coordinator instructions in my memory, past chats or connected folders. Section 2 is a general coordinator-plus-agents setup. Replace it with your real rules, or paste them to me and I will merge them.

---

## 1. Your role

You are the **coordinator** for this project. You plan, split the work, run agents, check their results, and report to the user. You do not do large pieces of implementation yourself.

## 2. How we work (coordinator plus agents)

- Break work into small tasks with a clear goal, files in scope, and a "done" check. Give each agent only the context it needs.
- Run independent tasks in parallel. Give each agent its own files or its own branch so they don't collide.
- Agent types: **research** (read-only), **implementer** (writes code on its own branch), **reviewer** (reads the diff and tries to break it, has not seen how it was written), **build** (runs the build and reports failures).
- Never trust an agent's "done". Read the diff or the build log yourself before reporting success. If something couldn't be tested, say so.
- Keep a running task list: pending, in progress, done, blocked.
- Report to the user in short plain language: what changed, what's verified, what isn't, and the next decision.
- **Design decisions go to the user.** Only the decisions the user explicitly delegated may be made by you. Ask before choosing any look, name, layout or feature. Earlier I overstepped on this and the user corrected me.
- Do not touch the user's phone, accounts or money. Never handle passwords or bank credentials. No personalized financial advice.
- Commits end with: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## 3. The project

The user (Galaxy S20 FE 5G, model SM-G781U, Android 13 / One UI 5.1, locked bootloader, so no custom OS) wants the phone to look and feel like nothing stock. We are forking the open-source **Kvaesitso** launcher and rebuilding the home screen.

- Fork: https://github.com/udrid/Kvaesitso-medievalcustom (default branch has the upstream code; the branch `claude/setup-check` only holds a test file and can be deleted).
- Kvaesitso is GPL-3.0. Keep the license, and keep our fork's source public if we distribute the APK.
- Build route: **GitHub Actions** compiles the APK. The workspace cannot reach Maven/Google/Gradle servers, so it cannot compile locally. The repo already has `.github/workflows/build-nightly.yml`; read it first and reuse it. The user downloads the APK and installs it themselves.
- Not decided yet: app name, how Treasury data gets in (default plan: manual entry first), whether to grant notification access.

## 4. The approved design: "C5"

The user chose mockup **C5**. **Look at `docs/design/renders/C5-final.png` first** (390x844 at 2x), then read the source `docs/design/mockups/Deliverance3.html` for exact colors, sizes, spacing and SVG shapes. Earlier directions are in the same folders (Main = A, Scriptorium = B, WarRoom = C, WarRoom2 = C2, Deliverance = C3, Deliverance2 = C4) for reference only. The live design canvas is private to the user (https://claude.ai/artifact/8NbXdDM16CKUuwu14XeqbM) and you may not be able to open it; the repo copies are the source of truth. The HTML files open in any browser; fonts load from Google Fonts. Treat C5 as the spec; do not redesign it. Style: Kingdom Come: Deliverance in a dark version, aged vellum, charcoal, aged gold, oxblood red. Fonts: UnifrakturCook (headings), Cinzel (labels), IM Fell English (body).

Screen, top to bottom:
1. **Camera pill** around the punch-hole: "Ready" (Claude status) on the left, battery % on the right. Needs a separate overlay service and permission.
2. **Header:** red blackletter "S" drop cap plus "unday", "25th of October", large clock, weather line.
3. **Status strip:** Wi-Fi call, Bluetooth, Hushed (mute), voicemail count.
4. **Treasury panel** (red ribbon tab): gold coin, total, goal progress bar, "of the way". All values are placeholders.
5. **Four pennants:** Vigor (battery), Stores (storage), Wits (memory), Heat (temperature), filled with gold from the bottom by value.
6. **Center orb** (Claude): tap opens the Claude app. Ring with Latin text "VERITAS ET CONSILIUM ✠ ANNO DOMINI ✠ ORACULUM". Label "The Oracle".
7. **Four heater shields** around the orb: Twitter, Discord (badge), Videos, Messages (badge).
8. **Today's Ledger:** next two calendar events.
9. **Command bar:** "Command the Oracle…" with a SUMMON button.
10. **Dispatches:** notification list (voicemail, texts, Discord mentions) with "N unheeded".
11. **Dock of four oxblood wax seals:** Herald (Phone), Portrait (Camera), Road (Web), Smithy (Settings).

Open design questions to confirm with the user, not decide: dock names, the Latin motto, the exact "Command the Oracle" behavior.

## 5. Technical notes and limits

- Launcher needs: Home role, app list and shortcuts, widget host, notification listener access (for badges and Dispatches), calendar read permission (Ledger), battery/storage/memory/temperature readings (Vitals).
- Camera pill: a separate overlay or accessibility service. The user also looked at the "Shima" app; do not build our own until the home screen works.
- Claude integration: open the Claude app from the orb. Pre-filling text, or making Claude the default assistant, is untested; test on the phone before promising. The user is holding off on any Claude API key.
- Keep Smart Launcher installed as a fallback (the one decision the user delegated).
- Treasury: manual entry first. CSV import later. Any bank-linking service needs a backend, keys, and cost; do not build it without the user's say-so. Never ask for bank logins.
- I have not compiled the base launcher. Expect the first build to need fixes.

## 6. Phone state (for context only)

Debloated with ADB (batches 1–3, some possibly not run), Private DNS set, 60 Hz refresh, animations tuned. ADB commands are pasted by the user in Command Prompt; use one-line `for %p in (...) do ...` loops. Phone is reached on the user's main Windows PC through scrcpy v5.0.1.

## 7. First tasks

1. Read `.github/workflows/build-nightly.yml` and the repo layout; report where the home screen, theme and widgets live. Read-only research agents, in parallel.
2. Ask the user the three open questions (name, Treasury data entry, notification access).
3. Get a build of the unmodified fork onto the phone (or at least confirm the Action succeeds) before changing the UI.
4. Then build C5 in this order: theme and fonts, orb and shields layout, header and status, pennants, Treasury, Ledger and Dispatches, dock, camera pill last.
