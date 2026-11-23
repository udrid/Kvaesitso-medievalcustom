# T24-ai-bridge: Oracle bridge (Claude controls the phone from the laptop)

Owner request 2026-11-23 (setup chat, laptop + phone remote control). The owner wants Claude, running in Claude Code on the laptop, to be the phone's main operator, from anywhere. Owner welcomes radical changes. No Claude API key (keyless).

## Scope (this branch)
1. New module `services/oracle-bridge`:
   - an accessibility service ("Oracle bridge") that reads the screen, taps, types, takes screenshots and lists notifications;
   - a tiny HTTP server on port 8765 that only answers Tailscale peers (100.64.0.0/10, fd7a:115c:a1e0::/48) and needs a Bearer secret.
2. Pairing over ADB at home: the laptop makes the secret and sends it through a broadcast receiver guarded by WRITE_SECURE_SETTINGS (only the ADB shell can send). Owner choice: automatic over ADB.
3. ADB always on at home: when Wi-Fi connects, set Settings.Global `adb_wifi_enabled` = 1. WRITE_SECURE_SETTINGS is granted once over ADB. Android only honours this on networks the owner trusted.
4. Notification while paired and on: "The Oracle watches" with an orb icon (owner choice).
5. Settings: "Oracle bridge On/Off" and a "forget laptop" row in the top settings group.
6. Laptop client: `C:\Users\clair\RemoteTools\agent.ps1` (not in this repo).

## Not in this branch
Claude's Scroll on Home (needs the owner's design picks), Treasury via SimpleFIN (T2, owner: net worth, quest = pay off debt).
