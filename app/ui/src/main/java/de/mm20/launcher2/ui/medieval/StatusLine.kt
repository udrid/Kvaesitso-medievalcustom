package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.media.AudioManager
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private class StatusFlags(val bluetoothOn: Boolean, val hushed: Boolean)

private fun readFlags(context: Context): StatusFlags {
    var bt = false
    try {
        bt = Settings.Global.getInt(context.contentResolver, "bluetooth_on", 0) == 1
    } catch (e: Exception) {
        Log.e("medieval", "Could not read bluetooth_on", e)
    }
    var hushed = false
    try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        hushed = am.ringerMode != AudioManager.RINGER_MODE_NORMAL
    } catch (e: Exception) {
        Log.e("medieval", "Could not read ringer mode", e)
    }
    return StatusFlags(bt, hushed)
}

@Composable
private fun StatusItem(label: String, icon: List<String>, iconSize: Float, color: androidx.compose.ui.graphics.Color, lit: Boolean) {
    Row(
        Modifier.alpha(if (lit) 1f else 0.4f),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SvgIcon(icon, iconSize, color, strokeWidth = 2f)
        Cap(label, size = 8f, color = if (lit) color else Medieval.TextFaint, spacingEm = 0.14f)
    }
}

/** Bluetooth and Hushed under the header divider (the weather now sits in the header). */
@Composable
internal fun StatusLine() {
    val context = LocalContext.current
    val flags by produceState(readFlags(context)) {
        while (true) {
            value = readFlags(context)
            delay(5_000L)
        }
    }
    Row(
        Modifier
            .at(22f, HeaderTop + 98f)
            .width(346.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusItem("Bluetooth", MedievalIcons.Bluetooth, 11f, Medieval.Gold, lit = flags.bluetoothOn)
        StatusItem("Hushed", MedievalIcons.Hushed, 12f, Medieval.Gold, lit = flags.hushed)
    }
}
