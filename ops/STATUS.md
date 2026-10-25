# STATUS (coordinator updates this before stopping)

| Task id | Stage | State | Thread/branch | Notes |
|---|---|---|---|---|
| T0-build | 1 | DONE (owner verified install, Home, Smart Launcher kept) | claude/T0-build | Brief at `tasks/T0-build/brief.md`. Fork lacks signing secrets; add a new push-triggered workflow |
| T1-home | 1 | DONE (owner verified install and taps 2026-10-25) | claude/T1-home | Home screen from C5, app name Deliverance |
| T1b-polish | 1 | active | claude/T1b-polish | Owner fixes: top rows, status line, app picker grid with search |
| T2-treasury | 1 | ready (T1 done; start after T1b to avoid same files) | - | Treasury via SimpleFIN Bridge (decisions.md). Verify the API with api-scout first |

## Open questions for the owner
None.

## Answered 2026-10-25 (see `ops/decisions.md`)
App name Deliverance; notification access yes; dock, motto and command bar delegated to Claude.
