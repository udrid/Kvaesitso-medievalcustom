# T24 Oracle bridge: owner checks on the phone

Most of this is checked by Claude from the laptop. Only steps 1-3 need you.

1. **See the orb notification.** Pull down the notification shade. You should see "The Oracle watches" with a small orb. Report: yes / no.
2. **Switch it off.** Open Deliverance settings (long-press the weekday). In the top group, set "Oracle bridge" to Off. The orb notification should disappear. Then set it back On.
3. **Restart check.** Restart the phone, unlock it, wait 1 minute. The orb notification should be back on its own.
4. Should NOT happen: no other app or device (not on Tailscale, not paired) can control the phone. Claude checks this from the laptop by calling without the secret (must be refused).

Checked by Claude on 2026-11-23 with the side-by-side test copy (de.mm20.launcher2.debug), over Tailscale:
- ping 0.4 s, screenshot 0.7 s (worked while a video played in landscape), screen reading 0.3 s, Home, open app, tap by text: worked.
- Typing into a text field: NOT yet confirmed (first try refused; fixed to look for any editable field; needs one more test).
- Notifications list: empty in the test copy because it has no notification access; the real launcher has it.
- ADB keep-alive: permission granted, adb_wifi_enabled = 1.
