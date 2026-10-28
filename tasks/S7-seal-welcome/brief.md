# Brief: S7-seal-welcome (stage 1, preview only)

Branch `claude/s7-seal-welcome-5d6384`, cut from `claude/integrate-6`. Role: `phone-stylist`.

Owner request (2026-10-28): "our lock page should be completely unique and changed. I want a creative way to unlock the phone."
Decision: the REAL lock screen stays Samsung's (no replacement lock app). Route: a BREAKING-THE-SEAL WELCOME, a one-shot animation when Home first appears after the phone is unlocked. It builds on the orb kindle after a wake (T9, `MedievalMotion.wakes`, `rememberOneShot`), not a duplicate.

Part 1 (preview only, no Kotlin):
1. How can the launcher tell "Home appears right after an unlock" with no new permission (USER_PRESENT, KeyguardManager.isKeyguardLocked, resume after keyguard)? Which is reliable on One UI 5; limits.
2. Three variants as animated HTML, `docs/design/S7-seal-welcome.html` + `.png`, 5 key frames each, 411 x 914 dp, Gold and Ember palettes, sharp C5 look, 1.2-1.8 s, small spark regions, no loop:
   A big wax seal over the orb cracks along the orb ring and falls away as the orb kindles;
   B the dock's four wax seals break in sequence, Home sections fade up, the orb kindles last;
   C a gold sigil is drawn around the orb ring, then the orb kindles.
   Each: battery cost, build cost, risk.
3. Rules: motion gate (`MedievalMotion.kt`), once per unlock, never delay touch (non-blocking overlay), setting "Welcome animation: On / Off" next to Animations.
4. Report READY FOR DESIGN OK with numbered choices, then STOP. Part 2 only after the coordinator forwards the owner's picks. State what is NOT verified.

Out of scope: replacing the lock screen, wallpaper/AOD (S3).
