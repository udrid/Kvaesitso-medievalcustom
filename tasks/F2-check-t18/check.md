# F2 final check: `claude/T18-contact-alerts`, checked on `claude/f2-check-t18-ac3950`

## VERDICT: PASS (after the 2 F2 fix commits on this branch), plus 1 ASK OWNER question that does not block the push
Push THIS branch (T18 + 3 F2 commits), not `claude/T18-contact-alerts` alone. Confidence: high for matching logic (unit tests with notifications built by the real Android builder), layout, colours and the motion gate (rendered); low for how Samsung apps actually build their notifications (phone only, see "Not verified").

## Fixes applied here (separate commits)
1. `dba0e26f1` should-fix, privacy: tapping a tile with no badge called `launchSafely(..., p.name)`, and `launchSafely` logs `"Could not open $what"` when it fails, so a contact's name could reach the log (`CommandLayerContent.kt` PersonCell). Now `"contact"`. Side effect: the error toast says "Could not open contact" instead of the name (only shown when the contact card cannot open).
2. `585dae8d1` should-fix, wrong contact: in a group chat every member in `android.people.list`, every older writer in the message history, and a group whose name equals a starred contact's name all got the badge. Now, for a group (`android.isGroupConversation`, or a conversation title as NotificationCompat reads it), only the writer of the newest message counts; no people list, no title. One-to-one chats unchanged. Test proves it: `groupChatCountsOnlyTheNewestWriter` fails on the T18 code and passes now.
3. `e71941184` test `F2ContactAlertChecks.kt` (8 matching tests, 8 pictures) + `docs/design/shots/F2/`.

## Checks
- SCOPE: 7 code files. Upstream `Notification.kt` in its own commit (`24c3f068c`): two defaulted fields at the end of the data class, filled in both constructors. All 3 upstream callers (`NotificationService.kt`) use the named constructors, so the listener, Dispatches and the shield badge are untouched. No manifest change, no new permission (READ_CONTACTS and notification access already used), no new dependency, no personal data, no KCD art.
- PRIVACY: the only logs are two tag-`medieval` error lines with fixed text (no names, numbers or notification text) + fix 1. Phone digits live only in memory.
- MATCHING (b), tests with notifications built by `Notification.Builder`: exact title (any case) -> that contact only; "Anna", "Anna Beck", "Anna Becker Jr" -> nobody; two starred "Anna"s -> each gets only their own; MessagingStyle sender with a `tel:` number under a nickname -> right contact; missed call by category and by a "missed" channel -> counts, an incoming call / e-mail / calendar / app update -> never; group chat -> fix 2. A non-starred person can never get a badge (only the up to 5 starred tiles exist).
- DISMISSALS (c): candidates are `asDispatches()` of the live repository list; a removed notification leaves the list, the count follows at once (test). Group summaries and non-clearable (ongoing) ones never count (test). Badge hidden at 0 (picture).
- MOTION (d)+(g): halo and swell use `rememberOneShot(..., LocalMotion.current.ambient)`, the same one-shot the Dispatches glow uses; nothing loops or polls. The one-line edit in `MedievalHomeComponent.kt` provides the Home's `motion` to `CommandOverlay`; before it the overlay got the default (Off). Nothing else in `CommandOverlay.kt` reads `LocalMotion`, so this edit changes no other behaviour. Pictures: gate open -> rings + swollen badge at 0.9 s; gate closed -> static badge, no rings; after 2.5 s -> nothing left behind.
- COLOURS (e): ring colour `Medieval.Glow`, rim `GoldDeep`, avatar `palette.avatarA/B`, badge = existing `Badge` (palette). All getters on `Medieval.palette` (a Compose state), so they follow a live palette change. Ember pictures show the warmer rings and rims.
- BUILD: `:app:ui:testDebugUnitTest --tests *ContactAlertShots* --tests *F2ContactAlertChecks*` (compiles app:ui) green offline, 23 tests. T18's 6 pictures re-render pixel-identical. Lint not run.
- SEE IT (f), looked at every picture: T18 `portrait-gold-0/1/12`, `portrait-ember-12`, `landscape-gold-12`, `landscape-ember-1`; F2 `portrait-gold-halo`, `portrait-ember-halo`, `portrait-gold-gate-closed`, `portrait-gold-halo-done`, `portrait-gold-99plus`, `landscape-ember-12`, `small-360x640-gold-12`, `portrait-font130-gold-12`. No overlap, the long name ends "Bartholome..." (font 130 %: "Bartholo..."), the Eve badge stays inside the frame at 360 dp, "99+" fits, the halo stays inside the panel and does not touch the ribbon.
- TESTLIST (T18's): step 1 was confusing ("send that starred person's number a text"), no star how-to for Samsung, no group-chat or should-NOT for non-message notifications from other apps. Rewritten as `tasks/F2-check-t18/testlist.md`.

## Nits (not fixed, low impact)
- The halo also replays when a chat app silently updates an existing notification (its post time moves). Seen only on the phone, if at all.
- The view model keeps the last list while the layer is closed, so a message that arrived while it was closed makes the halo play once just after opening (likely welcome); on the very first opening it can also play if contacts load before the notification list.
- At font 130 % the "12" fills the badge edge to edge (the badge does not grow with font size; same badge as the shields).
- Badge counts notifications, not messages (handoff says so).

## Not verified (phone only)
- How Samsung Messages, Google Messages, WhatsApp and the Samsung Phone app build their notifications: whether they carry a people list / sender, a title equal to the saved name, a missed-call category or channel. Most likely miss: Samsung's missed call shows "Missed call" as the title and the name only in the text; if it also has no people list, missed calls give NO badge. Then a small fix (match the text for missed calls) needs a probe on the phone first.
- A contact saved under a different name in WhatsApp than in Contacts matches only through a number.
- Halo look and timing on the real screen; tap opening the right conversation (`contentIntent`).
- Battery Saver closing the gate on the phone (code path same as the Dispatches glow, which the owner already has).

## ASK OWNER
1. A starred person writes in a GROUP chat (e.g. a family WhatsApp group). A) the badge shows on their tile, tap opens the group (recommended, what the branch does now); B) group chats never light a tile.
