# T6-summon Part 1: how to send text to the Claude Android app

Date: 2026-10-26. Sources are public web pages only. I did NOT decompile the APK and could NOT run anything on a phone.

## Ranked options

| # | Option | What it should do | Certainty |
|---|---|---|---|
| 1 | Share intent: `ACTION_SEND`, `text/plain`, `EXTRA_TEXT`, package `com.anthropic.claude` | Text lands in the Claude app's message box. Does NOT send by itself (no source says it does). | **likely** (one third-party report). From adb it reliably opened a new chat. From an app-like caller (an accessibility service) the text sometimes went into the chat that was already open instead. A launcher calls from an Activity, so it may behave like adb. Nobody has tested that. |
| 2 | `ACTION_PROCESS_TEXT` to `com.anthropic.claude/com.anthropic.claude.deeplink.DeepLinkActivity` | Same result as option 1, same caveat. | likely, same single report |
| 3 | `claude://` link or `https://claude.ai/code/new?q=...` | Prefills the composer, but of the **Claude Code** tab, not a normal chat. Needs Claude Code access on the account. | **VERIFIED that it exists** (Anthropic help article "Open the Claude mobile app with a link"); wrong target for normal questions. |
| 4 | `claude.ai/new?q=...` (normal chat) | Works in the browser. Whether the Android app grabs the link and opens a chat is unknown. | UNVERIFIED |
| 5 | Assistant integration (default assistant) | Nothing found. | UNVERIFIED, not pursuing |

Known gaps:
- No official page promises that the Claude Android app accepts shared text. The only evidence is the issue reported at [anthropics/claude-code#27739](https://github.com/anthropics/claude-code/issues/27739) (mirror: claudeissues.com), closed "not planned" on 2026-03-24 with no maintainer reply. That report also says the app adds shared text to the *current* chat in some cases and has no `claude://new-chat?text=` path.
- Auto-send: no source says any route sends the message by itself. Assume the owner must tap send in the Claude app.
- The app may have changed since February 2026.

Sources:
- https://claudeissues.com/issue/27739-android-app-no-reliable-way-to-start-new-conversation-via-share-intent
- https://support.claude.com/en/articles/14898120-open-the-claude-mobile-app-with-a-link

## Proposed probe (tiny, owner tests on phone)
Add a debug-only call (no new permission): from the SUMMON tap, build `Intent(ACTION_SEND)` with `type="text/plain"`, `EXTRA_TEXT="probe hello"`, `setPackage("com.anthropic.claude")`, `FLAG_ACTIVITY_NEW_TASK`, `startActivity` in a try/catch that logs with tag `medieval`. The owner checks three things: (a) does the Claude app open, (b) is it a NEW chat or the old one, (c) is the text in the box and not sent. Run it twice: once with the Claude app closed, once with an old chat open. Needs a build, so it waits for the owner's push OK. If (b) fails when an old chat is open, add `FLAG_ACTIVITY_CLEAR_TASK` as a second probe.

## Fallbacks if the probe fails (owner's multiple choice)
A. Copy the text to the clipboard, open the Claude app empty; owner pastes. Works for sure, one extra tap (long-press box, Paste).
B. Open claude.ai in the browser with the question in the link (`claude.ai/new?q=...`). Works in a browser, prefilled; uses the browser, not the app.
C. Share intent anyway (option 1) plus clipboard copy as a backup, so a paste is available if the text lands in the wrong chat.

## Questions for the owner
1. Which approach to build? Recommended: C (share intent plus clipboard backup), and I add the probe result to the testlist. Alternatives: A, B.
2. Is it fine that the owner must tap send inside the Claude app? (No route sends automatically.)

## NOT verified
- Everything above on a real phone, including whether `com.anthropic.claude` is the installed package name on the owner's S20 FE.
- The real manifest of the current Claude app (not decompiled).
- Whether the report from February 2026 still holds for the current app version.
