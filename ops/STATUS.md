# STATUS (coordinator updates this before stopping)

| Task id | Stage | State | Thread/branch | Notes |
|---|---|---|---|---|
| T0-build | 1 | active (waiting: owner must add 4 GitHub secrets, then push) | claude/T0-build | Brief at `tasks/T0-build/brief.md`. Fork lacks signing secrets; add a new push-triggered workflow |
| T1-home | 1 | blocked on T0 | - | Home screen layout from C5: theme, fonts, orb, shields. App name Deliverance |
| T2-treasury | 1 | blocked on T1 | - | Treasury via SimpleFIN Bridge (decisions.md). Verify the API with api-scout first |

## Open questions for the owner
None.

## Answered 2026-10-25 (see `ops/decisions.md`)
App name Deliverance; notification access yes; dock, motto and command bar delegated to Claude.
