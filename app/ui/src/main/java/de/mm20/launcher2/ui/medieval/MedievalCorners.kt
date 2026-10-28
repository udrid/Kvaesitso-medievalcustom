package de.mm20.launcher2.ui.medieval

import android.content.Context
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.component.preferences.ListPreference

/**
 * "Corner style" (T20): the shape of buttons and chips. Panels, cards, frames and ribbons are always sharp and
 * the wax-seal switch is always round; only buttons ([Button] and friends in MedievalButtons.kt) and the
 * typing-layer chips follow this choice.
 */
enum class CornerStyle { Sharp, Round }

/** The saved choice as Compose state, so every screen that reads it redraws when Settings changes it. */
object Corners {
    var style by mutableStateOf(CornerStyle.Sharp)
}

/** Provided by LauncherTheme; the default also reads [Corners], so screens outside the theme still follow. */
val LocalCornerStyle = compositionLocalOf { Corners.style }

/** Shape of a button (Round keeps the Material pill). */
internal fun CornerStyle.buttonShape(): Shape = if (this == CornerStyle.Round) CircleShape else RoundedCornerShape(0.dp)

/** Shape of a typing-layer chip. */
internal fun CornerStyle.chipShape(): Shape = if (this == CornerStyle.Round) RoundedCornerShape(16.dp) else RoundedCornerShape(0.dp)

private const val PrefsName = "medieval_home"
private const val CornerKey = "corner_style"

internal fun readCornerStyle(context: Context): CornerStyle {
    val name = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).getString(CornerKey, null)
    return CornerStyle.entries.firstOrNull { it.name == name } ?: CornerStyle.Sharp
}

internal fun writeCornerStyle(context: Context, style: CornerStyle) {
    context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit().putString(CornerKey, style.name).apply()
}

private var cornersLoaded = false

/** Reads the saved choice once per process (LauncherTheme calls it, like [loadPaletteOnce]). */
internal fun loadCornersOnce(context: Context) {
    if (cornersLoaded) return
    cornersLoaded = true
    Corners.style = readCornerStyle(context)
}

/** "Corner style" on Settings > Appearance: applied at once and saved. */
@Composable
fun MedievalCornerPreference() {
    val context = LocalContext.current
    remember { loadCornersOnce(context.applicationContext) }
    ListPreference(
        title = "Corner style",
        items = listOf(
            "Sharp: square corners on buttons and chips" to CornerStyle.Sharp,
            "Round: rounded buttons and chips" to CornerStyle.Round,
        ),
        value = Corners.style,
        onValueChanged = {
            Corners.style = it
            writeCornerStyle(context, it)
        },
    )
}
