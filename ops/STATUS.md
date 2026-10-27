# STATUS (coordinator updates this before stopping)

| Task id | Stage | State | Thread/branch | Notes |
|---|---|---|---|---|
| T0-build | 1 | DONE (owner verified install, Home, Smart Launcher kept) | claude/T0-build | Brief at `tasks/T0-build/brief.md`. Fork lacks signing secrets; add a new push-triggered workflow |
| T1-home | 1 | DONE (owner verified install and taps 2026-10-25) | claude/T1-home | Home screen from C5, app name Deliverance |
| T1b-polish | 1 | DONE (owner verified on phone 2026-10-25) | claude/T1b-polish | Status line, picker grid, Treasury overlap, ring text, ribbon border, Quest size; Actions run 40242634891 green. Thread archived. Owner moving to Android Studio (tasks/T1b-polish/local-build-setup.md); Studio installed, SDK not yet downloaded; C: has ~19 GB free |
| T3-dispatches | 1 | DONE (owner verified on phone 2026-10-26) | claude/T3-dispatches | Dispatches list, "N unheeded", shield badges. Needs notification access | Actions run 40263211930 green (last code commit "4"); owner chose swipe = dismiss for real
| T4-header | 1 | DONE (owner verified on phone 2026-10-26) | claude/T4-header | New header: date per weekday, time and weather small on right, assets +8% |
| T4b-fix | 1 | DONE (owner verified on phone 2026-10-26) | claude/T4b-fix | Fix off-center layout and dock under nav bar; weather setup. From T4 phone test |
| T4c-weather | 1 | DONE (owner verified: weather and scene show, 2026-10-26) | claude/T4c-weather | Weather never arrives (provider list shows only the German one). From T4b phone test |
| T5-pill | 1 | TESTED: top bar gone, pill, Wi-Fi, signal, notifications OK; position fix in T5b-fix | claude/T5-pill | Camera pill on Home: notification icons left; Hushed, Bluetooth, Wi-Fi, signal right; system top bar hidden on Home (design v5)|
| T5b-fix | 1 | FINISHED: READY TO PUSH, needs owner phone test (testlist.md) | claude/T5b-fix | Camera pill sits slightly too high | 
| T6-summon | 1 | FINISHED: READY TO PUSH, needs owner phone test (testlist.md) | claude/T6-summon | SUMMON button hands typed text to the Claude app |
| T5c-pill-position | 1 | FINISHED: READY TO PUSH (with T6), needs owner phone test + screenshot of the numbers (testlist.md); diagnostic commit 2e718ce8e to revert after | claude/T5c-pill-position | Pill still too high after two fixes. Goes into the T6 push |
| T8-header-size | 1 | READY TO PUSH (compiled locally, not seen on phone) | claude/T8-header-size | Larger clock, weather and scene; scene must not cross the divider |
| T10-cleanup | 1 | READY TO PUSH (compiled locally, not seen on phone); see testlist.md | claude/T10-cleanup | Remove pill numbers; planet medallion gap to weather on all 7 days |
| T2-treasury | 1 | deferred: owner wants it last (2026-10-25) | - | Treasury via SimpleFIN Bridge (decisions.md). Verify the API with api-scout first |

## Open questions for the owner
None.

## Answered 2026-10-25 (see `ops/decisions.md`)
App name Deliverance; notification access yes; dock, motto and command bar delegated to Claude.
