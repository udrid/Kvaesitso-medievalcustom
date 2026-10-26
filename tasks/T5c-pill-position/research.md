# Research: placing the pill exactly around the S20 FE camera hole

Certainty: HIGH = official doc or our own code, MED = one outside source or strong inference, LOW = guess.

## How Android describes the hole
- The OEM defines the cutout as an SVG-like path string, `config_mainBuiltInDisplayCutout`, in display pixels, native orientation, origin at the top centre of the display. Apps receive it as `DisplayCutout` (`boundingRects`, and on API 31+ `cutoutPath`). HIGH. Source: https://source.android.com/docs/core/display/display-cutouts
- `boundingRects` are the bounds of that path; `cutoutPath` is the path itself. So if the OEM path is a rectangle, the "path" gives exactly the same box as the rect: switching to the path (T5b-fix) changes nothing. MED (follows from the AOSP definition; Samsung's own value not found).
- OEMs often describe a punch hole as a rectangle that starts at the top edge of the screen and runs down to below the hole, not as a circle. Example found: a Xiaomi overlay `"M 0,0 H -33 V 132 H 33 V 0 H 0 Z"` (66 px wide, 0 to 132 px tall). MED (one example, other vendor). Source: https://github.com/misakazip-dev/android_device_xiaomi_klimt/pull/7
- Samsung's own value for SM-G781U: NOT found publicly (searched Samsung, GSMArena, AOSP, GitHub). Samsung firmware is the only source; a "No Cutout" module overrides this exact key on Samsung, so Samsung does use it. MED. Source: https://github.com/oneals365/No-Cutout-17

## S20 FE hole facts
- 1080 x 2400 panel, about 407 ppi, hole top centre, smaller than the other S20 models. HIGH for panel/ppi/position, no number for hole diameter or depth. Sources: https://www.gsmarena.com/samsung_galaxy_s20_fe-10428.php, https://m.gsmarena.com/samsung_galaxy_s20_fe_5g-review-2178p2.php, https://blog.bestbuy.ca/smartphones-accessories/cell-phones/samsung-galaxy-s20-fe-review
- Hole diameter, hole centre y, Samsung's cutout rect: no trustworthy public number. We do NOT use a per-device table.

## Coordinate space (our launcher)
- Theme sets `windowLayoutInDisplayCutoutMode = shortEdges` (`core/base/src/main/res/values/themes.xml:18,43`) and the activity calls `enableEdgeToEdge()` + `setDecorFitsSystemWindows(false)` (`SharedLauncherActivity.kt:96,115`). So the window covers the cutout and hiding the status bar does NOT move the window: window y=0 is screen y=0. HIGH (our code; AOSP: shortEdges always lets the window extend into a cutout on the short edge).
- Cutout rects from `rootWindowInsets` are in window coordinates; the pill subtracts `positionInWindow()` of its own box. Same space, consistent. HIGH (our code).
- Hiding the status bar does not remove the display cutout from the insets (cutout is not a system bar). MED.

## How others do it
- Camera-ring / "energy ring" style apps place their ring with the cutout bounds and give the user an offset/size slider, because OEM cutout values do not match the visible glass on every phone. MED (common pattern, no single authoritative source).

## Conclusion
The likely error is not the coordinate space but the shape Samsung reports: a box from the top edge (y=0) down to the hole. Its centre is half-way down that box, above the hole's centre, so the pill sits high. The path cannot help if it is the same box. Fix: when the outline touches the top edge and is taller than wide, treat the hole as a circle as wide as the box, sitting at the bottom of the box. The on-phone diagnostic shows the raw numbers to confirm or correct this.
