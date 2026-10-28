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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
private fun Pennant(x: Float, label: String, value: String, fraction: Float, fill: Color) {
    Box(Modifier.at(x, 64f).size(58.dp, 62.dp)) {
        Canvas(Modifier.size(58.dp, 62.dp)) {
            val k = size.width / 58f
            val path = Path().apply {
                moveTo(2f * k, 1f * k)
                lineTo(56f * k, 1f * k)
                lineTo(56f * k, 52f * k)
                lineTo(29f * k, 42f * k)
                lineTo(2f * k, 52f * k)
                close()
            }
            val stop = 1f - fraction.coerceIn(0f, 1f)
            val dark = Color(0xFF1A1610)
            drawPath(
                path,
                Brush.verticalGradient(
                    stop to dark, stop to fill, 1f to fill,
                    startY = 1f * k, endY = 52f * k,
                ),
            )
            drawPath(path, Medieval.GoldDeep, style = Stroke(1.5f * k))
        }
        MText(
            label, 7f, Color(0xFFD9C48C),
            Modifier.fillMaxWidth().padding(top = 9.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.16f, align = TextAlign.Center,
        )
        MText(
            value, 13f, Medieval.TextBright,
            Modifier.fillMaxWidth().padding(top = 22.dp),
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
    Pennant(PennantX[0], "VIGOR", v.batteryPercent?.toString() ?: "-", (v.batteryPercent ?: 0) / 100f, Color(0xFF7A5E2C))
    Pennant(PennantX[1], "STORES", v.storageFreePercent?.toString() ?: "-", (v.storageFreePercent ?: 0) / 100f, Color(0xFF5E4A24))
    Pennant(PennantX[2], "WITS", v.memoryFreePercent?.toString() ?: "-", (v.memoryFreePercent ?: 0) / 100f, Color(0xFF5E4A24))
    Pennant(PennantX[3], "HEAT", heatText, heatFraction, Color(0xFF6E2A1E))
}

/**
 * Pennant left edges. The flag shape inside the 58 wide box runs from x 2 to 56, so the outer
 * flags' visible edges meet the panel edges (16 and 345) and the gaps between the four are equal.
 */
private val PennantX = List(4) { 14f + it * (289f - 14f) / 3f }
