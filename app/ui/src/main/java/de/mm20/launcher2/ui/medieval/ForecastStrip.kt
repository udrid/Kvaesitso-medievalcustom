package de.mm20.launcher2.ui.medieval

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.weather.Forecast
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * S6 landscape: the Forecast strip between the shields and the dock. Six hours from now, read from the weather
 * the launcher already fetches (MedievalHomeVM.hourly). No new permission, no new fetch, nothing animates.
 * Fewer than six hours are shown as they are; with none, a quiet line says why. Numbers are never invented.
 */

private const val Cells = 6

private val CloudPath = "M7 15h10a4 4 0 0 0 0-8 6 6 0 0 0-11 2 3 3 0 0 0 1 6z"
private val SunPaths = listOf(svgCircle(12f, 12f, 4f), "M12 2v3M12 19v3M2 12h3M19 12h3M5 5l2 2M17 17l2 2M19 5l-2 2M7 17l-2 2")
private val MoonPaths = listOf("M20 14A8 8 0 0 1 10 4a8 8 0 1 0 10 10z")
private val CloudPaths = listOf("M7 18h10a4 4 0 0 0 0-8 6 6 0 0 0-11 2 3 3 0 0 0 1 6z")
private val PartlyPaths = listOf("M12 2v2M4.6 4.6L6 6M19.4 4.6L18 6M2 11h2", "M8 19h9a3.5 3.5 0 0 0 0-7 5 5 0 0 0-9.5 1.5A2.8 2.8 0 0 0 8 19z")
private val RainPaths = listOf(CloudPath, "M8 18l-1 3M12 18l-1 3M16 18l-1 3")
private val SnowPaths = listOf(CloudPath, "M8 19h.01M12 19h.01M16 19h.01M10 21.5h.01M14 21.5h.01")
private val BoltPaths = listOf(CloudPath, "M12 14l-2 4h3l-1 4")
private val FogPaths = listOf("M4 8h16M3 12h18M5 16h14M8 20h8")
private val WindPaths = listOf("M3 9h11a3 3 0 1 0-3-3M3 14h15a3 3 0 1 1-3 3M3 19h7")

/** Line-art icon paths for a forecast; moon instead of sun at night. */
private fun iconFor(f: Forecast): List<String> = when (f.icon) {
    Forecast.CLEAR, Forecast.EXTREME_HEAT, Forecast.EXTREME_COLD -> if (f.night) MoonPaths else SunPaths
    Forecast.PARTLY_CLOUDY, Forecast.MOSTLY_CLOUDY, Forecast.BROKEN_CLOUDS -> if (f.night) MoonPaths else PartlyPaths
    Forecast.FOG, Forecast.HAZE -> FogPaths
    Forecast.LIGHT_RAIN, Forecast.RAIN, Forecast.HEAVY_RAIN, Forecast.SLEET, Forecast.HAIL -> RainPaths
    Forecast.SNOW -> SnowPaths
    Forecast.THUNDER, Forecast.THUNDERSTORM, Forecast.HEAVY_THUNDERSTORM, Forecast.HEAVY_THUNDERSTORM_WITH_RAIN -> BoltPaths
    Forecast.WIND, Forecast.STORM -> WindPaths
    else -> CloudPaths
}

private fun degrees(kelvin: Double): String {
    val useFahrenheit = Locale.getDefault().country == "US"
    val deg = if (useFahrenheit) (kelvin - 273.15) * 9 / 5 + 32 else kelvin - 273.15
    return "${Math.round(deg)}°"
}

@Composable
private fun Cell(f: Forecast, hourLabel: String, width: Float) {
    Column(Modifier.width(width.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Cap(hourLabel, size = 7.5f, color = Medieval.GoldDeep, spacingEm = 0.1f)
        SvgIcon(iconFor(f), 15f, Medieval.Gold, strokeWidth = 1.5f)
        MText(degrees(f.temperature), 11f, Medieval.TextBright, font = Medieval.Cinzel, bold = true)
    }
}

/** The six-hour Forecast panel. Landscape only. */
@Composable
internal fun ForecastStrip(vm: MedievalHomeVM) {
    val context = LocalContext.current
    val now by rememberNow()
    val nowMs = now.toInstant().toEpochMilli()
    val shown = remember(vm.hourly, nowMs / 60_000L) {
        vm.hourly.filter { it.timestamp >= nowMs - 30 * 60_000L }.take(Cells)
    }
    val pattern = if (DateFormat.is24HourFormat(context)) "H:00" else "h a"
    val fmt = remember(pattern) { DateTimeFormatter.ofPattern(pattern, Locale.getDefault()) }
    val w = LocalColumnWidth.current
    val inner = w - 24f
    Panel(top = 0f, height = 58f, ribbon = "Forecast") {
        if (shown.isEmpty()) {
            MText(
                when (vm.weatherStatus) {
                    WeatherStatus.SetUp -> "Weather is not set up"
                    WeatherStatus.AllowLocation -> "Location is needed for the forecast"
                    WeatherStatus.Unavailable -> "Forecast unavailable"
                    WeatherStatus.NoCoverage -> "No forecast for your area"
                    WeatherStatus.Fetching -> "Fetching forecast…"
                    WeatherStatus.Ready -> "No hours ahead"
                },
                11f, Medieval.WeatherGrey, Modifier.at(12f, 24f).width(inner.dp), italic = true, align = TextAlign.Center,
            )
        } else {
            Row(Modifier.at(12f, 14f).width(inner.dp)) {
                shown.forEach { f ->
                    Cell(f, Instant.ofEpochMilli(f.timestamp).atZone(ZoneId.systemDefault()).format(fmt).uppercase(), inner / Cells)
                }
            }
        }
    }
}
