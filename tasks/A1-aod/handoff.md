# Handoff: A1-aod

What changed (new files only): `assets/aod/imageclock/aod-imageclock-<day|plain>-1200x900.png` (8 PNGs, 48 to 78 KB each), `docs/design/A1-aod.png` (contact sheet in a rounded frame with a mock clock), `assets/src/aod-sheet.html` (sheet source), `assets/src/render.html` (new `#ic-<day>` and `#ic-plain` modes, function `imageclock`), `tasks/A1-aod/testlist.md`, `assets/README.md` note.
Design: owner pick 2B. Medallion from `medal()` in lib.js, disc filled black, whole picture at 60 percent opacity over pure black #000. Blackletter day name plus small Cinzel planet name. Kept inside 96 px sides and 72 px top and bottom (checked by eye on the render).
Rebuild: `msedge --headless --hide-scrollbars --force-device-scale-factor=1 --window-size=1200,900 --screenshot=out.png "file:///.../assets/src/render.html#ic-mars"` (also sol, luna, mercury, jupiter, venus, saturn, plain). Sheet: `aod-sheet.html` at 1800 x 1000.
Looked at: contact sheet and the Mars picture on the PC.
NOT verified: how Samsung scales or crops the picture in the frame (the old full-screen 1080x2400 files stay but are the wrong shape for Image clock); real brightness on the panel; that "Image clock" shows Gallery pictures the way the screenshot suggests on this firmware; the 12 clock colours against the picture. The old `assets/aod/aod-*-1080x2400.png` are left in place.
