package de.mm20.launcher2.ui.medieval

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Locale

/** Phone vitals. A null field means the value could not be read (the pennant shows a dash). */
internal class Vitals(
    val batteryPercent: Int?,
    val storageFreePercent: Int?,
    val memoryFreePercent: Int?,
    val batteryTempC: Float?,
)

internal fun readVitals(context: Context): Vitals {
    var battery: Int? = null
    var temp: Float? = null
    try {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (intent != null) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) battery = level * 100 / scale
            val t = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            if (t != Int.MIN_VALUE) temp = t / 10f
        }
    } catch (e: Exception) {
        Log.e("medieval", "Could not read battery", e)
    }
    var storage: Int? = null
    try {
        val fs = StatFs(Environment.getDataDirectory().path)
        if (fs.totalBytes > 0) storage = (fs.availableBytes * 100 / fs.totalBytes).toInt()
    } catch (e: Exception) {
        Log.e("medieval", "Could not read storage", e)
    }
    var memory: Int? = null
    try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        if (info.totalMem > 0) memory = (info.availMem * 100 / info.totalMem).toInt()
    } catch (e: Exception) {
        Log.e("medieval", "Could not read memory", e)
    }
    return Vitals(battery, storage, memory, temp)
}

@Composable
internal fun rememberVitals(): State<Vitals> {
    val context = LocalContext.current
    return produceWhileResumed(readVitals(context)) {
        while (true) {
            value = readVitals(context)
            delay(5_000L)
        }
    }
}

@Composable
private fun Pennant(x: Float, label: String, value: String, fraction: Float, fill: Color, tall: Boolean = false) {
    // S6: landscape flags are 1.3 times as tall (same width), with a bigger number
    val h = if (tall) 80f else 62f
    Box(Modifier.at(x, 64f).size(58.dp, h.dp)) {
        Canvas(Modifier.size(58.dp, h.dp)) {
            val k = size.width / 58f
            val ky = size.height / 62f
            val path = Path().apply {
                moveTo(2f * k, 1f * k)
                lineTo(56f * k, 1f * k)
                lineTo(56f * k, 52f * ky)
                lineTo(29f * k, 42f * ky)
                lineTo(2f * k, 52f * ky)
                close()
            }
            val stop = 1f - fraction.coerceIn(0f, 1f)
            val dark = lerp(Medieval.Ink, Medieval.PanelTop, 0.85f) // 1A1712 in Gold and Oxblood, near-black in every palette
            drawPath(
                path,
                Brush.verticalGradient(
                    stop to dark, stop to fill, 1f to fill,
                    startY = 1f * ky, endY = 52f * ky,
                ),
            )
            drawPath(path, Medieval.GoldDeep, style = Stroke(1.5f * k))
        }
        MText(
            label, if (tall) 7.5f else 7f, Medieval.TextSoft,
            Modifier.fillMaxWidth().padding(top = if (tall) 12.dp else 9.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.16f, align = TextAlign.Center,
        )
        MText(
            value, if (tall) 18f else 13f, Medieval.TextBright,
            Modifier.fillMaxWidth().padding(top = if (tall) 28.dp else 22.dp),
            font = Medieval.Cinzel, bold = true, align = TextAlign.Center,
        )
    }
}

/** Vigor = battery, Stores = free storage, Wits = free memory, Heat = battery temperature. */
@Composable
internal fun Pennants() {
    val v by rememberVitals()
    val useFahrenheit = Locale.getDefault().country == "US"
    val heatText = v.batteryTempC?.let {
        val shown = if (useFahrenheit) it * 9f / 5f + 32f else it
        "${Math.round(shown)}°"
    } ?: "-"
    // gold rises from 20 C (empty) to 50 C (full)
    val heatFraction = v.batteryTempC?.let { (it - 20f) / 30f } ?: 0f
    val land = LocalLandscape.current
    val xs = if (land) LandscapePennantX else PennantX
    Box(if (land) Modifier.overflowTopStart().requiredSize(247.dp, 144.dp).graphicsLayer { scaleX = PennantsLandscapeScale; scaleY = PennantsLandscapeScale; transformOrigin = TransformOrigin(0f, 0f) } else Modifier) {
        Pennant(xs[0], "VIGOR", v.batteryPercent?.toString() ?: "-", (v.batteryPercent ?: 0) / 100f, Medieval.palette.pennantA, land)
        Pennant(xs[1], "STORES", v.storageFreePercent?.toString() ?: "-", (v.storageFreePercent ?: 0) / 100f, Medieval.palette.pennantB, land)
        Pennant(xs[2], "WITS", v.memoryFreePercent?.toString() ?: "-", (v.memoryFreePercent ?: 0) / 100f, Medieval.palette.pennantB, land)
        Pennant(xs[3], "HEAT", heatText, heatFraction, Medieval.palette.pennantC, land)
    }
}

/**
 * Pennant left edges. The flag shape inside the 58 wide box runs from x 2 to 56, so the outer
 * flags' visible edges meet the panel edges (16 and 345) and the gaps between the four are equal.
 */
private val PennantX = List(4) { 14f + it * (289f - 14f) / 3f }

/** Landscape (S1): the row is laid out 247 wide and drawn at 0.94 so it fills the 232 wide left column (S6). */
internal const val PennantsLandscapeScale = 0.94f
private val LandscapePennantX = List(4) { it * (247f - 58f) / 3f }
