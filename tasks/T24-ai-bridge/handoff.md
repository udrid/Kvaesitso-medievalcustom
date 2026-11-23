# T24 handoff

Branch `claude/T24-ai-bridge`, cut from `claude/integrate-9` (the build on the phone, Actions run 35).

- New module `services/oracle-bridge`: `OracleAgentService` (accessibility, hosts the server, ADB keep-alive, "The Oracle watches" notification), `BridgeServer` (HTTP on 8765, Tailscale peers only, Bearer secret, constant-time compare), `AgentActions` (endpoints), `OraclePairReceiver` (guarded by WRITE_SECURE_SETTINGS), `BridgePrefs`.
- Upstream touches (small): `settings.gradle.kts`, `app/app/build.gradle.kts`, `app/ui/build.gradle.kts` (one include line each), and `MainSettingsScreen.kt` (one import and one call).
- New UI file: `app/ui/.../medieval/OracleBridgeSettings.kt`.
- Laptop side (not in repo): `C:\Users\clair\RemoteTools\agent.ps1` (pair, ping, shot, ui, tap, type, key, open, notifs, adb).
- After installing the real build: run `agent.ps1 pair` at home (it grants WRITE_SECURE_SETTINGS), and enable the "Oracle bridge" accessibility service (Claude can do this over ADB).
- Open: typing confirmation; the Settings rows were not looked at on screen; Claude's Scroll and Treasury are separate tasks.
