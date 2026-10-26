package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.notifications.Notification
import de.mm20.launcher2.permissions.PermissionGroup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * The notifications that count as dispatches: dismissable ones with some text, no group
 * summaries (their children are listed instead). Newest first.
 */
internal fun List<Notification>.asDispatches(): List<Notification> =
    filter { it.isClearable && !it.isGroupSummary && (it.dispatchTitle() != null || it.dispatchText() != null) }
        .sortedByDescending { it.postTime }

// Not Notification.title/text: those use Bundle.getString and return null for styled text.
private fun Notification.dispatchTitle(): String? =
    extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString()?.takeIf { it.isNotBlank() }

private fun Notification.dispatchText(): String? =
    extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }

private fun timeLabel(context: Context, postTime: Long): String {
    val zoned = Instant.ofEpochMilli(postTime).atZone(ZoneId.systemDefault())
    if (zoned.toLocalDate() != LocalDate.now()) {
        return zoned.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm a"
    return zoned.format(DateTimeFormatter.ofPattern(pattern))
}

private fun appIcon(context: Context, packageName: String): ImageBitmap? = try {
    drawableToBitmap(context.packageManager.getApplicationIcon(packageName), 48).asImageBitmap()
} catch (e: Exception) {
    Log.e("medieval", "No icon for $packageName", e)
    null
}

@Composable
private fun DispatchRow(n: Notification, first: Boolean) {
    val context = LocalContext.current
    val icon = remember(n.packageName) { appIcon(context, n.packageName) }
    val title = n.dispatchTitle()
    val text = n.dispatchText()
    val line = listOfNotNull(title, text).joinToString(" · ")

    Row(
        Modifier.width(322.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Image(icon, null, Modifier.size(14.dp))
        } else {
            Box(Modifier.size(14.dp))
        }
        MText(
            line, 13f, if (first) Medieval.TextSoft else Medieval.TextDim,
            Modifier.weight(1f),
        )
        MText(timeLabel(context, n.postTime), 11f, Medieval.TextFaint, italic = true)
    }
}

/** Dispatches: the phone's active notifications, newest first. */
@Composable
internal fun Dispatches(vm: MedievalHomeVM) {
    val context = LocalContext.current
    val hasAccess by vm.hasNotificationAccess.collectAsState()
    val all by vm.notifications.collectAsState()
    val dispatches = remember(all) { all.asDispatches() }

    Panel(top = 612f, height = 90f, ribbon = "Dispatches") {
        if (hasAccess != false && dispatches.isNotEmpty()) {
            Cap(
                "${dispatches.size} unheeded", Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 8.dp),
                size = 8.5f, color = Medieval.RubricSoft, spacingEm = 0.18f,
            )
        }
        Column(
            Modifier.at(16f, 27f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            when {
                hasAccess == false -> MText(
                    "Tap to grant notification access", 13f, Medieval.TextSoft,
                    Modifier.clickable {
                        (context as? AppCompatActivity)?.let {
                            vm.permissionsManager.requestPermission(it, PermissionGroup.Notifications)
                        }
                    },
                )

                dispatches.isEmpty() -> MText("No dispatches", 13f, Medieval.TextDim)
                else -> dispatches.take(3).forEachIndexed { i, n -> DispatchRow(n, first = i == 0) }
            }
        }
    }
}
