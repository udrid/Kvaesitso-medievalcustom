# STATUS (coordinator updates this before stopping)

| Task id | Stage | State | Thread/branch | Notes |
|---|---|---|---|---|
| T0-build | 1 | DONE (owner verified install, Home, Smart Launcher kept) | claude/T0-build | Brief at `tasks/T0-build/brief.md`. Fork lacks signing secrets; add a new push-triggered workflow |
| T1-home | 1 | DONE (owner verified install and taps 2026-10-25) | claude/T1-home | Home screen from C5, app name Deliverance |
| T1b-polish | 1 | DONE (owner verified on phone 2026-10-25) | claude/T1b-polish | Status line, picker grid, Treasury overlap, ring text, ribbon border, Quest size; Actions run 40242634891 green. Thread archived. Owner moving to Android Studio (tasks/T1b-polish/local-build-setup.md); Studio installed, SDK not yet downloaded; C: has ~19 GB free |
| T3-dispatches | 1 | DONE (owner verified on phone 2026-10-26) | claude/T3-dispatches | Dispatches list, "N unheeded", shield badges. Needs notification access | Actions run 40263211930 green (last code commit "4"); owner chose swipe = dismiss for real
| T4-header | 1 | READY TO PUSH (Part 2 built, local compile of :app:ui green 2026-10-25; push + Actions build need owner OK) | claude/T4-header | New header: date per weekday, time and weather small on right, assets +8% |
| T2-treasury | 1 | deferred: owner wants it last (2026-10-25) | - | Treasury via SimpleFIN Bridge (decisions.md). Verify the API with api-scout first |

## Open questions for the owner
None.

## Answered 2026-10-25 (see `ops/decisions.md`)
App name Deliverance; notification access yes; dock, motto and command bar delegated to Claude.
