package de.mm20.launcher2.ui.medieval

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.BundleCompat
import de.mm20.launcher2.notifications.Notification

/**
 * T18: which active notifications come from a starred contact. Needs no permission beyond the notification
 * access and READ_CONTACTS the launcher already has. Names, numbers and texts are only compared, never logged.
 */

/** What is known about a starred contact for matching. [phoneDigits] are digits only. */
internal class ContactIdentity(val name: String, val lookupKey: String, val phoneDigits: Set<String>)

private const val CategoryMissedCall = "missed_call"
private const val ExtraPeopleList = "android.people.list"
private const val ExtraPeopleLegacy = "android.people"
private const val ExtraMessages = "android.messages"
private const val ExtraTemplate = "android.template"
private const val MessageSender = "sender"
private const val MessageSenderPerson = "sender_person"
private const val ExtraIsGroup = "android.isGroupConversation"
private const val ExtraConversationTitle = "android.conversationTitle"

/** A message or a missed call (category, MessagingStyle, or a channel the dialer names "missed"). */
internal fun Notification.isMessageOrMissedCall(): Boolean =
    category == android.app.Notification.CATEGORY_MESSAGE ||
        category == CategoryMissedCall ||
        channelId?.contains("missed", ignoreCase = true) == true ||
        extras.getString(ExtraTemplate)?.contains("MessagingStyle") == true ||
        extras.containsKey(ExtraMessages)

/** How a notification names a person: a uri (contact lookup, tel:) and/or a name. */
private class Sender(val uri: String?, val name: String?)

/** A group chat: Android's flag, or (apps built before it) a conversation title, as NotificationCompat reads it. */
private fun Notification.isGroupConversation(): Boolean =
    if (extras.containsKey(ExtraIsGroup)) extras.getBoolean(ExtraIsGroup)
    else extras.getCharSequence(ExtraConversationTitle) != null

private fun Bundle.messageSender(): Sender {
    val person = BundleCompat.getParcelable(this, MessageSenderPerson, android.app.Person::class.java)
    return Sender(person?.uri, person?.name?.toString() ?: getCharSequence(MessageSender)?.toString())
}

@Suppress("DEPRECATION")
private fun Notification.senders(): List<Sender> {
    val out = ArrayList<Sender>()
    if (isGroupConversation()) {
        // F2: in a group the people list holds every member and the history older writers; only the
        // writer of the newest message counts, and the title is the group's name
        try {
            val last = BundleCompat.getParcelableArray(extras, ExtraMessages, Parcelable::class.java)?.lastOrNull() as? Bundle
            if (last != null) out += last.messageSender()
        } catch (e: Exception) {
            android.util.Log.e("medieval", "Contact alerts: could not read the sender of a group notification", e)
        }
        return out
    }
    try {
        BundleCompat.getParcelableArrayList(extras, ExtraPeopleList, android.app.Person::class.java)
            ?.forEach { out += Sender(it.uri, it.name?.toString()) }
        // before Android 9 / apps that still use the old extra: uri strings
        when (val legacy = extras.get(ExtraPeopleLegacy)) {
            is Array<*> -> legacy.filterIsInstance<String>().forEach { out += Sender(it, null) }
            is Collection<*> -> legacy.filterIsInstance<String>().forEach { out += Sender(it, null) }
        }
        BundleCompat.getParcelableArray(extras, ExtraMessages, Parcelable::class.java)?.forEach { m ->
            (m as? Bundle)?.let { out += it.messageSender() }
        }
    } catch (e: Exception) {
        // a malformed extras bundle of some app: this notification just does not match
        android.util.Log.e("medieval", "Contact alerts: could not read the senders of a notification", e)
    }
    // last resort: the title is the sender's name in most chat and dialer notifications
    val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()
    if (!title.isNullOrBlank()) out += Sender(null, title)
    return out
}

private fun digitsOf(s: String): String = s.filter { it.isDigit() }

/** Compares the last 10 digits, so +1 555... and 555... are the same number. Short numbers never match. */
private fun sameNumber(a: String, b: String): Boolean =
    a.length >= 7 && b.length >= 7 && a.takeLast(10) == b.takeLast(10)

private fun Sender.isContact(c: ContactIdentity): Boolean {
    val u = uri
    if (u != null) {
        val parsed = Uri.parse(u)
        when (parsed.scheme) {
            "content" -> {
                // content://com.android.contacts/contacts/lookup/<key>/<id>
                val seg = parsed.pathSegments
                val i = seg.indexOf("lookup")
                if (i >= 0 && i + 1 < seg.size && seg[i + 1] == c.lookupKey) return true
            }

            "tel" -> {
                val d = digitsOf(parsed.schemeSpecificPart ?: "")
                if (c.phoneDigits.any { sameNumber(it, d) }) return true
            }
        }
    }
    val n = name?.trim()
    return !n.isNullOrEmpty() && n.equals(c.name.trim(), ignoreCase = true)
}

/**
 * The active message and missed-call notifications of each starred contact, by lookup key.
 * [candidates] should already be the dispatches (clearable, not group summaries), so a dismissed
 * notification stops counting at once. Contacts with none are absent from the map.
 */
internal fun contactAlerts(
    people: List<ContactIdentity>,
    candidates: List<Notification>,
): Map<String, List<Notification>> {
    if (people.isEmpty()) return emptyMap()
    val out = HashMap<String, MutableList<Notification>>()
    for (n in candidates) {
        if (!n.isMessageOrMissedCall()) continue
        val senders = n.senders()
        for (c in people) {
            if (senders.any { it.isContact(c) }) out.getOrPut(c.lookupKey) { ArrayList() } += n
        }
    }
    return out
}
