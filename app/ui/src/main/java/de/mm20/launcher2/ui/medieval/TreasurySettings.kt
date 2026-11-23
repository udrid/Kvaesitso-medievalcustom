package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.mm20.launcher2.ui.component.preferences.Preference
import kotlinx.coroutines.launch

/**
 * "Treasury" row in the launcher settings (T2): paste the SimpleFIN Setup Token once, or disconnect.
 * The owner makes the token on the SimpleFIN site; bank logins never pass through the launcher.
 */
@Composable
fun TreasuryPreference() {
    val context = LocalContext.current
    remember { TreasuryData.load(context) }
    val t by TreasuryData.state.collectAsState()
    var dialog by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Preference(
        title = if (t.connected) "Treasury: SimpleFIN connected (tap to disconnect)" else "Treasury: connect SimpleFIN",
        onClick = {
            if (t.connected) TreasuryData.disconnect(context) else { token = ""; message = null; dialog = true }
        },
    )
    if (dialog) {
        AlertDialog(
            onDismissRequest = { if (!busy) dialog = false },
            confirmButton = {
                TextButton(enabled = !busy && token.isNotBlank(), onClick = {
                    busy = true
                    scope.launch {
                        val err = TreasuryData.connect(context, token)
                        busy = false
                        if (err == null) dialog = false else message = err
                    }
                }) { Text(if (busy) "Connecting…" else "Connect") }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { dialog = false }) { Text("Cancel") } },
            title = { Text("Connect the Treasury") },
            text = {
                Column {
                    Text("On the SimpleFIN website, make a new Setup Token and copy it. Paste it here. It works only once.")
                    OutlinedTextField(value = token, onValueChange = { token = it }, singleLine = true, label = { Text("Setup Token") })
                    message?.let { Text(it) }
                }
            },
        )
    }
}
