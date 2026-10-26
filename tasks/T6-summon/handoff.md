# Handoff: T6-summon stage 1

Branch `claude/T6-summon`. Research in `research.md`; owner picked route C (share intent + clipboard, no auto-send).

What changed
- New `app/ui/.../ui/medieval/Summon.kt`: `summonToClaude(context, text)` copies the text to the clipboard, then starts `ACTION_SEND` `text/plain` `EXTRA_TEXT` with `setPackage("com.anthropic.claude")` + `FLAG_ACTIVITY_NEW_TASK`. Toasts: empty text "Write thy question first."; success "Copied. Paste if needed."; `ActivityNotFoundException` "The Claude app is not installed. Question copied."; other errors "Could not open the Claude app. Question copied." All caught errors are logged with tag `medieval`. `SummonButton` is the C5 button (dark red plate, gold hairline, Cinzel 10 bold, .18em).
- `CommandBar.kt`: SUMMON button at the right end of the Home bar. The Home bar is not a text field (a tap opens the launcher search), so SUMMON sends `SearchVM.searchQuery` (whatever is currently in the search); empty = hint only.
- Upstream edit (1 line, own commit): `LauncherSearchBar.kt` menu slot calls `SummonSearchBarButton()`, so SUMMON also shows inside the search box while it contains text. This is my addition so a question can actually be typed and summoned; owner may drop it.
- No new permissions. Search behaviour untouched.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` BUILD SUCCESSFUL.

NOT verified (workspace cannot run the app)
- Everything on the phone: that `com.anthropic.claude` accepts the share, opens a NEW chat (vs adding to the open one), puts the text in the box and does not send. The only evidence is a third-party report (anthropics/claude-code#27739) about calls from an accessibility service.
- That the SUMMON button inside the search box fits next to the filter icon, and that the Home button looks right on the bar.
- That `searchQuery` is empty when Home is shown (if search text lingers, Home SUMMON will send the old text).
- Android 13's own "copied" popup may show together with our toast.
