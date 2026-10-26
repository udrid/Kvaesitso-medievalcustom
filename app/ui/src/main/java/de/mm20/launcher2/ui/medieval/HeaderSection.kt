package de.mm20.launcher2.ui.medieval

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mm20.launcher2.weather.Forecast
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

/** Top of the T4 header on the stage; the divider sits 80 below it and the status row 98 below it. */
internal const val HeaderTop = 30f

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

private val CapShadow = Color(0xFF1C0605)
private val CapGlow = Color(0x29A8352B)
private val CapFill = Brush.verticalGradient(
    0f to Color(0xFFD9574A),
    0.38f to Color(0xFFB03A2E),
    0.66f to Color(0xFF6E1710),
    1f to Color(0xFF9A2F25),
)

/**
 * Handpainted first letter: dark offset shadow with a faint red glow, an uneven gold ink edge
 * (two outlines a hair apart stand in for the design's wobble filter) and an oxblood gradient fill.
 */
@Composable
private fun DropCap(letter: String) {
    val base = TextStyle(fontFamily = Medieval.Gothic, fontSize = 68.sp, lineHeight = 68.sp)
    val px = LocalDensity.current.density
    Box {
        Text(
            letter, Modifier.offset(2.dp, 2.dp),
            style = base.copy(color = CapShadow, shadow = Shadow(CapGlow, Offset.Zero, 7f * px)),
        )
        Text(letter, Modifier.offset(0.4.dp, 0.3.dp), style = base.copy(color = Medieval.GoldDeep, drawStyle = Stroke(2.2f * px)))
        Text(letter, style = base.copy(color = Medieval.Gold, drawStyle = Stroke(2.6f * px)))
        Text(letter, style = base.copy(brush = CapFill))
    }
}

/** T4 header: planetary day line, handpainted weekday, date, medallion, clock, weather and its little scene. */
@Composable
internal fun HeaderSection(forecast: Forecast?) {
    val context = LocalContext.current
    val now: ZonedDateTime by rememberNow()
    val planet = planetOf(now.dayOfWeek)
    val dayName = now.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    val month = now.month.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    val date = "${ordinal(now.dayOfMonth)} of $month"
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm"
    val clock = now.format(DateTimeFormatter.ofPattern(pattern))
    val medallionParts = remember(planet) { medallion(planet) }
    val ruleParts = remember(planet) { dayRule(planet) }

    Box(Modifier.at(22f, HeaderTop).size(346.dp, 80.dp)) {
        Cap("Day of ${planet.label}", size = 8.5f, color = Medieval.GoldDeep, spacingEm = 0.3f)
        Row(Modifier.offset(y = 10.dp)) {
            DropCap(dayName.take(1))
            Box(Modifier.padding(start = 6.dp)) {
                Text(
                    dayName.drop(1), Modifier.offset(y = 16.dp), maxLines = 1,
                    style = TextStyle(
                        fontFamily = Medieval.Gothic, fontSize = 29.sp, lineHeight = 29.sp,
                        color = Medieval.Parchment, shadow = Shadow(Color.Black, Offset(0f, LocalDensity.current.density), 0f),
                    ),
                )
                Cap(date, Modifier.offset(y = 54.dp), size = 10f, spacingEm = 0.2f)
            }
        }
        VectorCanvas(
            medallionParts, 88f, Modifier.offset(204.dp, 2.dp).size(88.dp, 70.dp),
            originX = -44f, originY = -35f,
        )
        Column(
            Modifier.align(Alignment.TopEnd).offset(y = 9.dp),
            horizontalAlignment = Alignment.End,
        ) {
            MText(clock, 25f, Medieval.TextBright)
            MText(
                weatherLine(forecast), 11f, Medieval.WeatherGrey, Modifier.padding(top = 5.dp).width(64.dp),
                italic = true, align = TextAlign.End,
            )
            WeatherScene(forecast, now.toInstant().toEpochMilli(), Modifier.padding(top = 3.dp))
        }
    }
    VectorCanvas(ruleParts, 346f, Modifier.at(22f, HeaderTop + 80f).size(346.dp, 14.dp))
}
