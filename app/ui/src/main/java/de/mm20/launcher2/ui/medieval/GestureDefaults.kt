package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.util.Log
import de.mm20.launcher2.preferences.GestureAction
import de.mm20.launcher2.preferences.ui.GestureSettings

/**
 * S5 (owner 2026-10-28): swipe up opens the apps page; swipe left/right, double tap and long press do nothing by
 * default. The phone already has saved values (double tap was Lock screen), so the new defaults are written ONCE;
 * the owner can still pick anything in Settings > Gestures afterwards. Swipe down is not touched here: the launcher
 * ignores it in code (SharedLauncherActivity).
 */
internal object GestureDefaults {
    private const val Prefs = "medieval_home"
    private const val Done = "s5_gesture_defaults"

    fun applyOnce(context: Context, settings: GestureSettings) {
        val prefs = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        if (prefs.getBoolean(Done, false)) return
        try {
            settings.setSwipeUp(GestureAction.Widgets())
            settings.setSwipeLeft(GestureAction.NoAction)
            settings.setSwipeRight(GestureAction.NoAction)
            settings.setDoubleTap(GestureAction.NoAction)
            settings.setLongPress(GestureAction.NoAction)
            prefs.edit().putBoolean(Done, true).apply()
        } catch (e: Exception) {
            Log.e("medieval", "Could not write the S5 gesture defaults", e)
        }
    }
}
