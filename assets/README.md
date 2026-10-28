# S3 assets (owner 2026-10-27)

Final images (1080 x 2400 unless noted):
- `wallpaper/deliverance-orb-1080x2400.png`: pick 1C, for the lock screen. Home keeps its own ground (pick H1).
- `aod/aod-<day>-1080x2400.png`: pick 2B, seven days. Pure black ground, no clock drawn (Samsung draws its own).
- `keyboard/swiftkey-vellum-1080x1000.png`: pick 6A, for SwiftKey's photo theme.
- `icons/sheets/*.png`: rendered icon sheets, cut into the pack by the generator below.
Sources are in `src/`: `lib.js` (palette, medallions, orb), `emblems.js` (43 emblems, plates), `render.html`, `icons.html`, `mapping.tsv` (app to emblem), `build-iconpack.ps1` (cuts the sheets, writes appfilter.xml and drawable.xml into `iconpack/`).

Re-render (Edge headless, from this folder; fonts load from `app/ui/src/main/res/font`):
`msedge --headless --hide-scrollbars --force-device-scale-factor=1 --window-size=1080,2400 --screenshot=out.png "file:///.../assets/src/render.html#wall"` (also `#aod-sol` to `#aod-saturn`, `#keyboard` with height 1000).
Icon sheets: `icons.html#heater|seal|bare` at 1536 x 1152 with `--default-background-color=00000000`; `icons.html#fallback-heater|seal` at 192 x 192.
Then run the commands in `build-iconpack.ps1` (it needs PowerShell; a signed-scripts policy may require pasting them into a console).
