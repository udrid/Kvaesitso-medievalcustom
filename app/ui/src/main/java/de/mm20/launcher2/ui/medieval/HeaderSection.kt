package de.mm20.launcher2.ui.medieval

import android.text.format.DateFormat
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import java.time.LocalDate
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
import de.mm20.launcher2.ui.settings.SettingsActivity
import de.mm20.launcher2.weather.Forecast
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale

/** Top of the T4 header on the stage; the divider sits 80 below it and the status row 98 below it. */
internal const val HeaderTop = 30f

/** T16: the body sits this many stage units closer to the header; Dispatches grows and the dock moves down by the same amount (in body units) so the dock stays put. */
internal const val BodyLift = 8f
internal const val BodyScaleF = 1.08f

private fun ordinal(day: Int): String {
    val suffix = if (day in 11..13) "th" else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$day$suffix"
}

private fun weatherTemp(f: Forecast): String {
    val kelvin = f.temperature
    val useFahrenheit = Locale.getDefault().country == "US"
    val deg = if (useFahrenheit) (kelvin - 273.15) * 9 / 5 + 32 else kelvin - 273.15
    return "${Math.round(deg)}° · "
}

/**
 * T17: short forms for every condition word the weather code produces (core/i18n strings
 * weather_condition_*, English), each at most 11 letters. Anything with "thunder" in it becomes Storm.
 * Other languages and plugin texts have no short form and are only shrunk.
 */
private val ShortCondition = mapOf(
    "clear sky" to "Clear", "cloudy" to "Cloudy", "fair" to "Fair", "fog" to "Fog", "hail" to "Hail",
    "haze" to "Haze", "partly cloudy" to "Part cloudy", "wind" to "Wind", "unknown" to "Unknown",
    "thunder" to "Thunder", "thunderstorm" to "Storm",
    "rain" to "Rain", "light rain" to "Drizzle", "heavy rain" to "Downpour",
    "rain showers" to "Showers", "light rain showers" to "Showers", "heavy rain showers" to "Downpour",
    "sleet" to "Sleet", "light sleet" to "Sleet", "heavy sleet" to "Sleet",
    "sleet showers" to "Sleet", "light sleet showers" to "Sleet", "heavy sleet showers" to "Sleet",
    "snow" to "Snow", "light snow" to "Flurries", "heavy snow" to "Snowfall",
    "snow showers" to "Snow", "light snow showers" to "Flurries", "heavy snow showers" to "Snowfall",
)

internal fun shortCondition(condition: String): String {
    val key = condition.trim().lowercase(Locale.ENGLISH)
    return ShortCondition[key] ?: if ("thunder" in key) "Storm" else condition
}

private const val WeatherMaxSp = 12.5f
private const val WeatherMinSp = 10f

/** Weather line that fits [width] whole: full text shrunk to 10 sp, else the short form shrunk to 10 sp, else ellipsis. */
@Composable
private fun WeatherLine(f: Forecast, width: androidx.compose.ui.unit.Dp, modifier: Modifier) {
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val density = LocalDensity.current
    val temp = weatherTemp(f)
    val fit = remember(f.temperature, f.condition, density) {
        val maxPx = with(density) { width.toPx() }
        fun widthAt(text: String, sp: Float) = measurer.measure(
            text,
            TextStyle(fontFamily = Medieval.FellEnglish, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = sp.sp),
            maxLines = 1, softWrap = false,
        ).size.width
        fun biggest(text: String): Float? {
            var sp = WeatherMaxSp
            while (sp >= WeatherMinSp) {
                if (widthAt(text, sp) <= maxPx) return sp
                sp -= 0.5f
            }
            return null
        }
        val full = temp + f.condition
        val short = temp + shortCondition(f.condition)
        biggest(full)?.let { return@remember full to it }
        biggest(short)?.let { return@remember short to it }
        short to WeatherMinSp
    }
    MText(fit.first, fit.second, Medieval.WeatherGrey, modifier.width(width), italic = true, align = TextAlign.End, lineHeight = 14f)
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
private fun DropCap(letter: String, shimmer: Float = 0f) {
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
        if (shimmer > 0f) {
            // T17 new-day shimmer: a soft gold band sweeps over the red letter, clamped to the letter shape by the text brush
            val w = with(LocalDensity.current) { 68.sp.toPx() }
            val c = w * (-0.4f + 1.8f * shimmer)
            val band = w * 0.28f
            val peak = Color(0xFFF3D27A).copy(alpha = 0.85f * bump(shimmer, 0f, 1f))
            Text(
                letter,
                style = base.copy(
                    brush = Brush.linearGradient(
                        0f to Color.Transparent, 0.5f to peak, 1f to Color.Transparent,
                        start = Offset(c - band, 0f), end = Offset(c + band, band * 0.8f),
                    ),
                ),
            )
        }
    }
}

/** T4 header: planetary day line, handpainted weekday, date, medallion, clock, weather and its little scene. */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun HeaderSection(
    forecast: Forecast?,
    status: WeatherStatus,
    onAllowLocation: (AppCompatActivity) -> Unit,
) {
    val context = LocalContext.current
    val now: ZonedDateTime by rememberNow()

    // T17 new-day shimmer: once per new calendar day, on the first sight of it with the motion gate open.
    // The date is recorded either way, so a closed gate (Battery Saver, Off, ...) never replays it later.
    val motion = LocalMotion.current
    val ambient = rememberUpdatedState(motion.ambient)
    var newDays by remember { mutableIntStateOf(0) }
    var resumes by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { resumes++ }
    LaunchedEffect(now.toLocalDate(), resumes) {
        withFrameMillis { } // let the motion gate settle after a resume
        val prefs = context.getSharedPreferences("medieval_home", Context.MODE_PRIVATE)
        val shown = prefs.getString("last_day", null)
        val today = LocalDate.now().toString()
        if (shown == today) return@LaunchedEffect
        prefs.edit().putString("last_day", today).apply()
        if (shown != null && ambient.value) newDays++
    }
    val shimmer by rememberOneShot(newDays, 1_500L, motion.ambient)
    val planet = planetOf(now.dayOfWeek)
    val dayName = now.dayOfWeek.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    val month = now.month.getDisplayName(DateTextStyle.FULL, Locale.getDefault())
    val date = "${ordinal(now.dayOfMonth)} of $month"
    val pattern = if (DateFormat.is24HourFormat(context)) "H:mm" else "h:mm"
    val clock = now.format(DateTimeFormatter.ofPattern(pattern))
    val medallionParts = remember(planet) { medallion(planet) }
    val ruleParts = remember(planet) { dayRule(planet) }

    if (LocalLandscape.current) {
        // S1 landscape: the same pieces in the 222 wide left column: day line, weekday and date with the
        // medallion on the right, then clock on the left and weather with its scene on the right.
        Box(
            Modifier.size(222.dp, 144.dp)
                .combinedClickable(onClick = {}, onLongClick = { launchSafely(context, settingsIntent(context, null), "launcher settings") }),
        ) {
            Cap("Day of ${planet.label}", size = 8.5f, color = Medieval.GoldDeep, spacingEm = 0.3f)
            Row(Modifier.offset(y = 10.dp)) {
                DropCap(dayName.take(1), shimmer)
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
                medallionParts, 88f, Modifier.offset(168.dp, (-2).dp).size(54.dp, 43.dp),
                originX = -44f, originY = -35f,
            )
            Row(Modifier.offset(y = 84.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
                MText(clock, 29f, Medieval.TextBright, lineHeight = 31f)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    if (forecast != null) {
                        WeatherLine(forecast, 108.dp, Modifier.padding(top = 2.dp))
                    } else {
                        val setUp = status == WeatherStatus.SetUp || status == WeatherStatus.AllowLocation
                        MText(
                            when (status) {
                                WeatherStatus.SetUp -> "Tap to set up weather"
                                WeatherStatus.AllowLocation -> "Allow location"
                                WeatherStatus.Unavailable -> "Weather unavailable"
                                WeatherStatus.NoCoverage -> "No weather for your area"
                                else -> "Fetching weather…"
                            },
                            11f, Medieval.WeatherGrey,
                            Modifier.padding(top = 2.dp).width(108.dp)
                                .let {
                                    if (setUp) it.clickable {
                                        val activity = context as? AppCompatActivity
                                        if (status == WeatherStatus.AllowLocation && activity != null) {
                                            onAllowLocation(activity)
                                        } else {
                                            launchSafely(context, settingsIntent(context, SettingsActivity.ROUTE_WEATHER_INTEGRATION), "weather settings")
                                        }
                                    } else it
                                },
                            italic = true, align = TextAlign.End, maxLines = 2, lineHeight = 13f,
                        )
                    }
                    WeatherScene(forecast, now.toInstant().toEpochMilli(), Modifier.padding(top = 3.dp))
                }
            }
        }
        VectorCanvas(ruleParts, 346f, Modifier.at(0f, 130f).size(222.dp, 9.dp))
        return
    }

    // Owner choice 2026-10-26: long-press the header opens the launcher settings.
    Box(
        Modifier.at(22f, HeaderTop).size(346.dp, 80.dp)
            .combinedClickable(onClick = {}, onLongClick = { launchSafely(context, settingsIntent(context, null), "launcher settings") }),
    ) {
        Cap("Day of ${planet.label}", size = 8.5f, color = Medieval.GoldDeep, spacingEm = 0.3f)
        Row(Modifier.offset(y = 10.dp)) {
            DropCap(dayName.take(1), shimmer)
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
            medallionParts, 88f, Modifier.offset(185.dp, 2.dp).size(88.dp, 70.dp),
            originX = -44f, originY = -35f,
        )
        Column(
            Modifier.align(Alignment.TopEnd),
            horizontalAlignment = Alignment.End,
        ) {
            MText(clock, 29f, Medieval.TextBright, lineHeight = 31f)
            val needsSetup = forecast == null
            if (forecast != null) WeatherLine(forecast, 74.dp, Modifier.padding(top = 2.dp)) else MText(
                when (status) {
                    WeatherStatus.SetUp -> "Tap to set up weather"
                    WeatherStatus.AllowLocation -> "Allow location"
                    WeatherStatus.Unavailable -> "Weather unavailable"
                    WeatherStatus.NoCoverage -> "No weather for your area"
                    else -> "Fetching weather…"
                },
                if (needsSetup) 11.5f else 12.5f, Medieval.WeatherGrey,
                Modifier.padding(top = 2.dp).width(if (needsSetup) 84.dp else 74.dp)
                    .let {
                        if (needsSetup) it.clickable {
                            val activity = context as? AppCompatActivity
                            if (status == WeatherStatus.AllowLocation && activity != null) {
                                onAllowLocation(activity)
                            } else {
                                launchSafely(context, settingsIntent(context, SettingsActivity.ROUTE_WEATHER_INTEGRATION), "weather settings")
                            }
                        } else it
                    },
                italic = true, align = TextAlign.End, maxLines = if (needsSetup) 2 else 1, lineHeight = if (needsSetup) 13f else 14f,
            )
            WeatherScene(forecast, now.toInstant().toEpochMilli(), Modifier.padding(top = 3.dp))
        }
    }
    VectorCanvas(ruleParts, 346f, Modifier.at(22f, HeaderTop + 80f).size(346.dp, 14.dp))
}

/** Opens the launcher's own settings, or one of its screens when [route] is given. */
internal fun settingsIntent(context: android.content.Context, route: String?): Intent =
    Intent(context, SettingsActivity::class.java).apply {
        if (route != null) putExtra(SettingsActivity.EXTRA_ROUTE, route)
    }
