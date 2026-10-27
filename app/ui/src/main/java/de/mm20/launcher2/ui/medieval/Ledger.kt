package de.mm20.launcher2.ui.medieval

import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.permissions.PermissionGroup
import de.mm20.launcher2.search.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun timeLabel(context: android.content.Context, event: CalendarEvent): String {
    val start = event.startTime ?: return ""
    val zoned = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault())
    if (event.allDay) {
        // All-day events have no clock time, so show the date they fall on (e.g. "Oct 31").
        return zoned.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }
    if (zoned.toLocalDate() != LocalDate.now()) {
        return zoned.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm"
    return zoned.format(DateTimeFormatter.ofPattern(pattern))
}

/** How long before an event its cross pulses (T9). */
private const val SoonMs = 15 * 60_000L

@Composable
private fun LedgerRow(
    time: String,
    title: String,
    first: Boolean,
    onClick: (() -> Unit)? = null,
    pulse: State<Float>? = null,
) {
    Row(
        Modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it },
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // T9: the red cross pulses twice (2.2 s) when its event is 15 minutes away
        MText(
            "✠", 12f, if (first) Medieval.Rubric else Medieval.GoldDark,
            Modifier.graphicsLayer {
                val p = pulse?.value ?: 0f
                val s = 1f + 0.5f * bump(p, 0f, 0.6f) + 0.25f * bump(p, 0.6f, 1f)
                scaleX = s
                scaleY = s
            },
        )
        MText(
            time, 10f, if (first) Medieval.Gold else Medieval.TextFaint,
            Modifier.width(42.dp), font = Medieval.Cinzel, bold = true,
        )
        MText(title, 13f, if (first) Medieval.TextSoft else Medieval.TextDim)
    }
}

/** Today's Ledger: the next two calendar events. */
@Composable
internal fun Ledger(vm: MedievalHomeVM) {
    val context = LocalContext.current
    val hasPermission by vm.hasCalendarPermission.collectAsState()
    val now = System.currentTimeMillis()
    val upcoming = vm.events.filter { it.endTime >= now }.take(2)

    // T9: while Home is on screen, check every 30 s for an event starting within 15 minutes; each event pulses once
    val motion = LocalMotion.current
    val pulsed = remember { mutableSetOf<String>() }
    var pulseKey by remember { mutableStateOf<String?>(null) }
    var pulses by remember { mutableIntStateOf(0) }
    val pulse = rememberOneShot(pulses, 2_200L, motion.ambient)
    LaunchedEffect(upcoming.map { it.key }, motion.ambient) {
        if (!motion.ambient) return@LaunchedEffect
        while (true) {
            val t = System.currentTimeMillis()
            val due = upcoming.firstOrNull { e ->
                val start = e.startTime
                !e.allDay && start != null && start - t in 0..SoonMs && e.key !in pulsed
            }
            if (due != null) {
                pulsed += due.key
                pulseKey = due.key
                pulses++
            }
            delay(30_000L)
        }
    }

    Panel(top = 330f, height = 68f, ribbon = "Today's Ledger") {
        Column(
            Modifier.at(16f, 25f),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            when {
                hasPermission == false -> LedgerRow(
                    "", "Tap to grant calendar access", first = true,
                    onClick = {
                        (context as? AppCompatActivity)?.let {
                            vm.permissionsManager.requestPermission(it, PermissionGroup.Calendar)
                        }
                    },
                )

                upcoming.isEmpty() -> LedgerRow("", "No tidings", first = false)
                else -> upcoming.forEachIndexed { i, e ->
                    LedgerRow(
                        timeLabel(context, e), e.label, first = i == 0,
                        pulse = if (e.key == pulseKey) pulse else null,
                    )
                }
            }
        }
    }
}
