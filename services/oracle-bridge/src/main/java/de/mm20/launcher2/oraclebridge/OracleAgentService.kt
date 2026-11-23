package de.mm20.launcher2.oraclebridge

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

/**
 * Oracle bridge agent (T24). An accessibility service so it can read the screen, tap, type and take
 * screenshots, and so Android keeps it running without a foreground service. It hosts [BridgeServer],
 * which only answers the paired laptop over Tailscale.
 */
class OracleAgentService : AccessibilityService() {

    private var server: BridgeServer? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    @Volatile var lastPackage: String? = null
        private set

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            event.packageName?.toString()?.let { lastPackage = it }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = WeakReference(this)
        refreshServer()
        watchWifi()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopServer()
        networkCallback?.let {
            try {
                getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it)
            } catch (e: IllegalArgumentException) {
                Log.e(Tag, "unregister network callback", e)
            }
        }
        networkCallback = null
        instance = null
        return super.onUnbind(intent)
    }

    fun isListening(): Boolean = server?.isRunning() == true

    /** Start or stop the server to match the settings (paired and enabled). */
    fun refreshServer() {
        val secret = BridgePrefs.secret(this)
        if (secret != null && BridgePrefs.isEnabled(this)) {
            if (server?.isRunning() != true) {
                server = BridgeServer(this, secret, BridgePrefs.Port).also { it.start() }
            } else if (server?.secret != secret) {
                stopServer()
                server = BridgeServer(this, secret, BridgePrefs.Port).also { it.start() }
            }
            showWatchNotification(true)
        } else {
            stopServer()
            showWatchNotification(false)
        }
    }

    private fun stopServer() {
        server?.stop()
        server = null
    }

    /** "The Oracle watches" (owner's wording, 2026-11-23): shown while the laptop can control the phone. */
    private fun showWatchNotification(show: Boolean) {
        val nm = getSystemService(NotificationManager::class.java)
        if (!show) {
            nm.cancel(NotificationId)
            return
        }
        nm.createNotificationChannel(
            NotificationChannel(ChannelId, getString(R.string.oracle_channel), NotificationManager.IMPORTANCE_LOW)
        )
        val n = Notification.Builder(this, ChannelId)
            .setSmallIcon(R.drawable.ic_oracle_orb)
            .setContentTitle(getString(R.string.oracle_watches))
            .setContentText(getString(R.string.oracle_watches_detail))
            .setOngoing(true)
            .build()
        try {
            nm.notify(NotificationId, n)
        } catch (e: SecurityException) {
            Log.e(Tag, "notification permission missing", e)
        }
    }

    /** Keep ADB wireless debugging on: whenever Wi-Fi connects, ask for it again. Android only honours it on trusted networks. */
    private fun watchWifi() {
        val cm = getSystemService(ConnectivityManager::class.java)
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) ensureAdbWifi(this@OracleAgentService)
            }
        }
        cm.registerDefaultNetworkCallback(cb)
        networkCallback = cb
    }

    companion object {
        private const val ChannelId = "oracle_bridge"
        private const val NotificationId = 7101
        private var instance: WeakReference<OracleAgentService>? = null
        fun getInstance(): OracleAgentService? = instance?.get()

        /** Sets Settings.Global adb_wifi_enabled = 1. Needs WRITE_SECURE_SETTINGS (granted once over ADB). */
        fun ensureAdbWifi(context: Context): String {
            if (!BridgePrefs.keepAdb(context)) return "off in settings"
            return try {
                if (Settings.Global.getInt(context.contentResolver, AdbWifiSetting, 0) != 1) {
                    Settings.Global.putInt(context.contentResolver, AdbWifiSetting, 1)
                    "turned on"
                } else "already on"
            } catch (e: SecurityException) {
                Log.e(Tag, "adb_wifi_enabled: WRITE_SECURE_SETTINGS not granted", e)
                "no permission"
            }
        }

        private const val AdbWifiSetting = "adb_wifi_enabled"
    }
}
