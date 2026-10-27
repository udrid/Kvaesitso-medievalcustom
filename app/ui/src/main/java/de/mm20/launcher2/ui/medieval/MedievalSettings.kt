package de.mm20.launcher2.ui.medieval

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.mm20.launcher2.ui.component.preferences.ListPreference

/** "Animations: Full / Minimal / Off" in the launcher settings (T9). Saved with the other Home choices. */
@Composable
fun MedievalAnimationsPreference() {
    val context = LocalContext.current
    var level by remember { mutableStateOf(readAnimLevel(context)) }
    ListPreference(
        title = "Animations",
        items = listOf(
            "Full: orb, reactions and the ring light" to AnimLevel.Full,
            "Minimal: orb and reactions" to AnimLevel.Minimal,
            "Off: nothing moves" to AnimLevel.Off,
        ),
        value = level,
        onValueChanged = {
            level = it
            writeAnimLevel(context, it)
        },
    )
}
