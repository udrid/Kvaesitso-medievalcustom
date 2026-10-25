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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
    if (event.allDay) return "Day"
    val zoned = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault())
    if (zoned.toLocalDate() != LocalDate.now()) {
        return zoned.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm"
    return zoned.format(DateTimeFormatter.ofPattern(pattern))
}

@Composable
private fun LedgerRow(time: String, title: String, first: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it },
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MText("✠", 12f, if (first) Medieval.Rubric else Medieval.GoldDark)
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

    Panel(top = 476f, height = 68f, ribbon = "Today's Ledger") {
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
                    LedgerRow(timeLabel(context, e), e.label, first = i == 0)
                }
            }
        }
    }
}
