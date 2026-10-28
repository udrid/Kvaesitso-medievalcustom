# T19-live-palette: header follows the palette at once
Bug (owner, phone test of the combined build): after choosing a colour scheme Home recolours at once EXCEPT the header (weekday block, drop cap, date, planet medallion, divider, weather area), which keeps the old colours until the phone is rotated.
Task: find every place in `app/ui/.../ui/medieval/` that reads a palette colour once (remember without key, cached brush/path, top-level val) and make it follow `Medieval.palette`. Prove it with a Robolectric screenshot test that switches the palette inside one composition. No look changes. Keep the fix small.
Done: compile green, testlist, handoff, lessons, STATUS row, READY TO PUSH. Opus final check before any push.
