package de.mm20.launcher2.ui.medieval

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.weather.Forecast
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun ordinal(day: Int): String {
    val suffix = if (day in 11..13) "th" else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$day$suffix"
}

internal fun weatherLine(f: Forecast?): String {
    if (f == null) return "[weather]"
    val kelvin = f.temperature
    val useFahrenheit = Locale.getDefault().country == "US"
    val deg = if (useFahrenheit) (kelvin - 273.15) * 9 / 5 + 32 else kelvin - 273.15
    return "${Math.round(deg)}° · ${f.condition}"
}

/** Drop cap, date and big clock (C5 header). The weather line sits in the status row. */
@Composable
internal fun HeaderSection() {
    val context = LocalContext.current
    val now: ZonedDateTime by rememberNow()
    val dayName = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = now.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val date = "${ordinal(now.dayOfMonth)} of $month"
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm"
    val clock = now.format(DateTimeFormatter.ofPattern(pattern))

    Row(
        Modifier
            .at(22f, 54f)
            .width(346.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.Top) {
            MText(
                dayName.take(1), 46f, Medieval.Rubric,
                font = Medieval.Blackletter, bold = true,
            )
            Column(Modifier.padding(top = 4.dp)) {
                MText(
                    dayName.drop(1), 21f, Medieval.Parchment,
                    font = Medieval.Blackletter, bold = true,
                )
                Cap(date, Modifier.padding(top = 5.dp))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            MText(clock, 40f, Medieval.TextBright)
        }
    }
}

