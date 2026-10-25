# Gate

A stage passes only if all hold. The workspace cannot compile, so the build runs in GitHub Actions.
1. Push the branch; the build workflow for it finishes green (`gh run list` / `gh run watch`, or the Actions tab).
2. No new `catch` without logging; no new upstream-file edits outside the task brief.
3. Diff reviewed by the coordinator (and `reviewer` for medium/high risk).
4. `tasks/<id>/testlist.md` exists and follows the owner rules.
