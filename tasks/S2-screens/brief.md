# S2-screens brief (owner, 2026-10-27)

Thread: S2-screens, acting as the `phone-stylist` agent. Branch claude/S2-screens (worktree claude/s2-screens-1156ca).

## Task
Carry the Deliverance (C5) design to the rest of the LAUNCHER's own screens as ONE BATCH:
1. Search results page and cards under the typing layer (apps, contacts, calendar, files, settings, web, tasks) and the card details / result sheet.
2. Launcher settings (SettingsActivity and sub-screens: lists, switches, sliders, dialogs, our Animations entry).
3. Widget/feed page left of Home and the add-widget flow.
4. Bottom sheets, dialogs, menus, snackbars, app picker.
5. Assistant activity and any other activity.
Find what Kvaesitso's theme system restyles centrally (colour scheme, shapes, typography, transparencies; ImportThemeActivity) so most screens change with the fewest edits. Say which screens still need custom drawing (ribbons, double-gold frames, grain/vignette). Respect the battery/motion rules in the design system.

## Part 1 (preview only)
`docs/design/S2-screens.html` + `.png` (390 wide): BEFORE and AFTER per screen (before is a drawing, labelled), two variants where a real choice exists (A theme-level restyle + few ribbons, B full custom frames). Cheaper variant, battery and build risk stated. Mark [M] parts and update `ops/monetization.md`. Commit locally, report READY FOR DESIGN OK with numbered choices, STOP.

## Part 2
Only after the coordinator forwards the owner's picks.

## Out of scope
Home screen, wallpaper / lock screen / icon pack assets (another thread). Never push; never start a build.
