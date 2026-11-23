package de.mm20.launcher2.oraclebridge

import android.content.Context

/**
 * Oracle bridge settings (T24). Stored in app-private preferences, never in Settings.Global
 * (which any app can read). The secret only ever arrives from the laptop over ADB.
 */
object BridgePrefs {
    private const val Name = "oracle_bridge"
    private const val KeySecret = "secret"
    private const val KeyEnabled = "enabled"
    private const val KeyKeepAdb = "keep_adb"

    /** Port the bridge listens on (Tailscale addresses only). */
    const val Port = 8765

    private fun prefs(context: Context) = context.getSharedPreferences(Name, Context.MODE_PRIVATE)

    fun secret(context: Context): String? = prefs(context).getString(KeySecret, null)?.takeIf { it.length >= 32 }

    fun setSecret(context: Context, secret: String?) {
        prefs(context).edit().putString(KeySecret, secret).apply()
    }

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KeyEnabled, true)

    fun setEnabled(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(KeyEnabled, on).apply()
        OracleAgentService.getInstance()?.refreshServer()
    }

    /** Turn ADB wireless debugging back on whenever Wi-Fi connects (Android itself only allows it on trusted networks). */
    fun keepAdb(context: Context): Boolean = prefs(context).getBoolean(KeyKeepAdb, true)

    fun setKeepAdb(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(KeyKeepAdb, on).apply()
    }

    fun isPaired(context: Context): Boolean = secret(context) != null
}
