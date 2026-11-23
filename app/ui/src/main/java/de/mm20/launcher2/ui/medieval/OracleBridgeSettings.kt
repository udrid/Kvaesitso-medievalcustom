package de.mm20.launcher2.ui.medieval

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.mm20.launcher2.oraclebridge.BridgePrefs
import de.mm20.launcher2.oraclebridge.OracleAgentService
import de.mm20.launcher2.ui.component.preferences.ListPreference
import de.mm20.launcher2.ui.component.preferences.Preference

/**
 * "Oracle bridge" in the launcher settings (T24): lets the paired laptop (Claude, over Tailscale) control the phone.
 * Pairing itself happens from the laptop over ADB; here the owner can switch it off or forget the laptop.
 */
@Composable
fun OracleBridgePreference() {
    val context = LocalContext.current
    var on by remember { mutableStateOf(BridgePrefs.isEnabled(context)) }
    var paired by remember { mutableStateOf(BridgePrefs.isPaired(context)) }
    ListPreference(
        title = "Oracle bridge",
        items = listOf(
            "On: your paired laptop can control this phone" to true,
            "Off: nobody can" to false,
        ),
        value = on,
        onValueChanged = {
            on = it
            BridgePrefs.setEnabled(context, it)
        },
    )
    val agentRunning = OracleAgentService.getInstance() != null
    Preference(
        title = when {
            !agentRunning -> "Oracle bridge is asleep: tap to wake it in Accessibility"
            !paired -> "No laptop paired yet"
            else -> "Laptop paired: tap to forget it"
        },
        onClick = {
            if (!agentRunning) {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } else if (paired) {
                BridgePrefs.setSecret(context, null)
                OracleAgentService.getInstance()?.refreshServer()
                paired = false
            }
        },
    )
}
