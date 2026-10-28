# Handoff: T18-contact-alerts

Branch `claude/T18-contact-alerts` (cut from `claude/integrate-4`). Starred contacts in the typing layer's People panel get a red count badge and a one-shot gold halo when a message or missed call is waiting.

Changes
- Upstream (own commit): `data/notifications/.../Notification.kt` now also keeps `category` and `channelId` (two defaulted fields).
- NEW `medieval/ContactAlerts.kt`: `contactAlerts(people, notifications)`. Only message / missed-call notifications count (category `msg` or `missed_call`, channel id containing "missed", MessagingStyle template, or `android.messages` extra). Sender found by: `android.people.list` Person uri (contact lookup uri or `tel:` number, last 10 digits compared), legacy `android.people` strings, MessagingStyle message sender Person/name, then exact (case-insensitive) title = display name. Candidates are `asDispatches()` (clearable, no group summary, has text), so a dismissed notification stops counting. No call log, no new permission. Nothing is logged except tag-`medieval` errors.
- `CommandLayerContent.kt`: `Person` now holds lookup key + phone digits (extra `Phone` query per starred contact, READ_CONTACTS already used); `CommandLayerVM.notifications` from T3's `NotificationRepository` (no second listener); `PersonCell` draws the badge (reused `Badge`, now internal, "99+" cap), the halo (same rings as the Dispatches halo, circular, 1.8 s, `rememberOneShot` + `rememberRises`) and the badge swell. Tap with a badge = `openDispatch` of the newest matching notification (reused from Dispatches, now internal); without = contact card as before.
- `MedievalHomeComponent.kt` (one line): `LocalMotion` is now provided around `CommandOverlay` (before the layer was outside the gate and would never have animated).
- Test: `ContactAlertShots.kt` (pictures in `docs/design/shots/T18/`: 0, 1, 12 unread, long name, Gold and Ember, portrait and landscape; plus a matching-rules check, passes). `StarredForTests` hook in `CommandLayerContent.kt` for fake contacts.

Checked: `./gradlew :app:ui:compileDebugKotlin --offline` green; `ContactAlertShots` tests pass; pictures looked at (badge 12 / 3 / 1, "Bartholome..." ellipsis, Ember palette fine). The halo is NOT in the pictures (motion gate is closed in the test).

NOT verified
- Everything on the phone. Matching depends on how Samsung Messages, Google Messages, WhatsApp and the dialer build their notifications: people list present or not, title = saved name or a number, a missed-call category/channel. A contact saved under a different name than the app shows will not match unless a phone number/uri is supplied.
- Group-summary-only notifications (Samsung) may show no badge (same limit as Dispatches).
- The halo and swell look and timing; the tile's badge position at 99+.
- Badge 12 does not mean 12 messages: it counts notifications, and a chat app may fold many messages into one.
- Number matching uses the last 10 digits; two numbers sharing them would match.
