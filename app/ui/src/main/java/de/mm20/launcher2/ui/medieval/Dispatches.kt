package de.mm20.launcher2.ui.medieval

import android.app.PendingIntent
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
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

private val GlowGold = Color(0xFFE5B25A)

/** Swipe distance (stage px) after which a dispatch is dismissed. */
private const val DismissDistance = 90f

/** Opens what the notification opens; falls back to the app itself. Never logs notification text. */
private fun openDispatch(context: Context, n: Notification) {
    val intent = n.contentIntent
    if (intent != null) {
        try {
            intent.send()
            return
        } catch (e: PendingIntent.CanceledException) {
            Log.e("medieval", "Dispatch intent of ${n.packageName} was cancelled", e)
        }
    }
    val launch = context.packageManager.getLaunchIntentForPackage(n.packageName)
    if (launch != null) launchSafely(context, launch, n.packageName)
}

@Composable
private fun DispatchRow(n: Notification, first: Boolean, onOpen: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val icon = remember(n.packageName) { appIcon(context, n.packageName) }
    val title = n.dispatchTitle()
    val text = n.dispatchText()
    val line = listOfNotNull(title, text).joinToString(" · ")
    var drag by remember(n.key) { mutableFloatStateOf(0f) }

    Row(
        Modifier
            .width((LocalColumnWidth.current - if (LocalLandscape.current) 24f else 64f).dp)
            .graphicsLayer {
                translationX = drag
                alpha = 1f - (abs(drag) / (3f * DismissDistance)).coerceIn(0f, 0.6f)
            }
            .pointerInput(n.key) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(drag) > DismissDistance) onDismiss() else drag = 0f
                    },
                    onDragCancel = { drag = 0f },
                    onHorizontalDrag = { _, delta -> drag += delta },
                )
            }
            .clickable(onClick = onOpen),
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
    // T9: a new dispatch makes the panel glow once (1.8 s), only while Home is on screen
    val glow = rememberOneShot(
        rememberRises(dispatches.maxOfOrNull { it.postTime } ?: 0L), 1_800L, LocalMotion.current.ambient,
    )

    Panel(top = 454f, height = 90f + BodyLift / BodyScaleF, ribbon = "Dispatches") {
        Box(Modifier.matchParentSize().drawBehind {
            val k = bump(glow.value, 0f, 1f)
            if (k <= 0f) return@drawBehind
            // a soft gold halo just outside the frame, made of fading rings
            val step = 1.5.dp.toPx()
            for (i in 1..8) {
                val o = step * i
                drawRect(
                    GlowGold.copy(alpha = 0.45f * k * (1f - i / 9f) / 3f),
                    topLeft = Offset(-o, -o),
                    size = Size(size.width + 2 * o, size.height + 2 * o),
                    style = Stroke(step),
                )
            }
        })
        if (hasAccess != false && dispatches.isNotEmpty()) {
            Cap(
                "${dispatches.size} unheeded", Modifier.align(Alignment.TopEnd).padding(end = if (LocalLandscape.current) 12.dp else 16.dp, top = 8.dp),
                size = 8.5f, color = Medieval.RubricSoft, spacingEm = 0.18f,
            )
        }
        Column(
            Modifier.at(if (LocalLandscape.current) 12f else 16f, 27f),
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
                else -> dispatches.take(3).forEachIndexed { i, n ->
                    DispatchRow(
                        n, first = i == 0,
                        onOpen = { openDispatch(context, n) },
                        onDismiss = { vm.dismissNotification(n) },
                    )
                }
            }
        }
    }
}
