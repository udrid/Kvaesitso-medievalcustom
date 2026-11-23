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

## Added in the same push: Claude's Scroll (T25) and Treasury (T2)
5. **Scroll.** Claude writes a test message from the laptop. Look at Dispatches on Home: the first line should be that message in gold italics with a small gold orb. Tap it: a framed "The Oracle's scroll" window lists the messages. Swipe the line sideways: it goes away.
6. **Treasury before connecting.** Home still shows "[YOUR TOTAL]" and "Quest: [GOAL]" (nothing invented).
7. **Treasury after connecting** (only after you sign up on SimpleFIN): settings → "Treasury: connect SimpleFIN", paste the Setup Token, tap Connect. On Home the total shows "••••". Tap the coin: your net worth appears, then hides again after 10 seconds. The bar says "Quest: pay off debt" with a %.
8. Should NOT happen: the total must never show without tapping the coin.

Checked by Claude on 2026-11-23 (test copy): Scroll message written over Tailscale and shown as the first Dispatches line (landscape Home). Typing into the Oracle bar: the field accepted the text (no error). Treasury: compiled only; NOT tested with a real SimpleFIN account (needs the owner's sign-up). UNVERIFIED: whether credit card and loan balances come back negative (debt = negative balances).
