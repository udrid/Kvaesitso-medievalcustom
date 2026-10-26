package de.mm20.launcher2.ui.medieval

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

private const val Tag = "medieval"

/** What the camera pill shows besides notifications. [signal] is 0..4 bars, or -1 when unavailable. */
internal class PillStatus(
    val wifi: Boolean,
    val signal: Int,
    val bluetooth: Boolean,
    val hushed: Boolean,
)

/**
 * Wi-Fi, mobile signal, Bluetooth and Hushed, kept current by listeners that exist only while
 * the launcher is started (registered on ON_START, removed on ON_STOP). No polling.
 */
@Composable
internal fun rememberPillStatus(): PillStatus {
    val context = LocalContext.current.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    var wifi by remember { mutableStateOf(false) }
    var signal by remember { mutableIntStateOf(-1) }
    var bluetooth by remember { mutableStateOf(false) }
    var hushed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, context) {
        var stop: (() -> Unit)? = null
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START && stop == null) {
                stop = startWatching(
                    context,
                    { wifi = it },
                    { signal = it },
                    { bluetooth = it },
                    { hushed = it },
                )
            } else if (event == Lifecycle.Event.ON_STOP) {
                stop?.invoke()
                stop = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stop?.invoke()
            stop = null
        }
    }
    return PillStatus(wifi, signal, bluetooth, hushed)
}

private fun startWatching(
    context: Context,
    setWifi: (Boolean) -> Unit,
    setSignal: (Int) -> Unit,
    setBluetooth: (Boolean) -> Unit,
    setHushed: (Boolean) -> Unit,
): () -> Unit {
    val main = Handler(Looper.getMainLooper())
    val stops = mutableListOf<() -> Unit>()

    // Wi-Fi: is the default network a Wi-Fi network (ACCESS_NETWORK_STATE, already declared)
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            setWifi(caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        }

        override fun onLost(network: Network) {
            setWifi(false)
        }
    }
    try {
        cm.registerDefaultNetworkCallback(netCallback, main)
        stops += { cm.unregisterNetworkCallback(netCallback) }
    } catch (e: Exception) {
        Log.e(Tag, "Pill: could not watch the network", e)
    }

    // Mobile signal bars (AF probe: TelephonyCallback.SignalStrengthsListener, API 31+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cb = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                setSignal(signalStrength.level)
            }
        }
        try {
            tm.registerTelephonyCallback(context.mainExecutor, cb)
            stops += { tm.unregisterTelephonyCallback(cb) }
        } catch (e: SecurityException) {
            Log.e(Tag, "Pill: signal bars need a permission we do not hold", e)
        } catch (e: Exception) {
            Log.e(Tag, "Pill: could not watch the signal", e)
        }
    } else {
        Log.w(Tag, "Pill: signal bars need Android 12 or newer")
    }

    // Bluetooth on/off (Settings.Global, readable without permission)
    val btUri = Settings.Global.getUriFor("bluetooth_on")
    fun readBluetooth() = try {
        setBluetooth(Settings.Global.getInt(context.contentResolver, "bluetooth_on", 0) == 1)
    } catch (e: Exception) {
        Log.e(Tag, "Pill: could not read bluetooth_on", e)
    }
    readBluetooth()
    val btObserver = object : ContentObserver(main) {
        override fun onChange(selfChange: Boolean) {
            readBluetooth()
        }
    }
    try {
        context.contentResolver.registerContentObserver(btUri, false, btObserver)
        stops += { context.contentResolver.unregisterContentObserver(btObserver) }
    } catch (e: Exception) {
        Log.e(Tag, "Pill: could not watch bluetooth_on", e)
    }

    // Hushed: ringer silent or vibrate, or any Do Not Disturb filter
    fun readHushed() = try {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val filter = nm.currentInterruptionFilter
        setHushed(
            am.ringerMode != AudioManager.RINGER_MODE_NORMAL ||
                (filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
                    filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN)
        )
    } catch (e: Exception) {
        Log.e(Tag, "Pill: could not read the ringer mode", e)
    }
    readHushed()
    val hushReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            readHushed()
        }
    }
    try {
        ContextCompat.registerReceiver(
            context,
            hushReceiver,
            IntentFilter().apply {
                addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
                addAction(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        stops += { context.unregisterReceiver(hushReceiver) }
    } catch (e: Exception) {
        Log.e(Tag, "Pill: could not watch the ringer mode", e)
    }

    return {
        stops.forEach {
            try {
                it()
            } catch (e: Exception) {
                Log.e(Tag, "Pill: could not remove a listener", e)
            }
        }
    }
}
