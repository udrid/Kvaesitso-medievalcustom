package de.mm20.launcher2.ui.medieval

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import de.mm20.launcher2.notifications.Notification
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.appcompat.app.AppCompatActivity
import de.mm20.launcher2.permissions.PermissionGroup
import de.mm20.launcher2.permissions.PermissionsManager
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.search.Application
import de.mm20.launcher2.searchable.PinnedLevel
import de.mm20.launcher2.services.favorites.FavoritesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val Tag = "medieval"

/**
 * T14: what the typing layer shows before anything is typed (docs/design/T14-layer.html, variant C):
 * most used apps, then one panel with starred contacts and quick switches. Sections with nothing
 * to show are left out.
 */
@Composable
internal fun CommandLayerContent(modifier: Modifier = Modifier) {
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }

    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            // S1 landscape: only about 200 high above the keyboard, so two panes side by side
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) { MostUsedApps(tick) }
                Column(Modifier.weight(1f)) { PeopleAndSwitches(tick) { tick++ } }
            }
        } else {
            MostUsedApps(tick)
            PeopleAndSwitches(tick) { tick++ }
        }
    }
}

// ---- Most used apps ------------------------------------------------------------------------

/** Launch counts of the taps this fork handles itself (Home shields, dock, this layer). */
internal object UsageCounter {
    private const val Prefs = "medieval_usage"

    fun bump(context: Context, packageName: String) {
        if (packageName == context.packageName) return
        val p = context.getSharedPreferences(Prefs, Context.MODE_PRIVATE)
        p.edit().putInt(packageName, p.getInt(packageName, 0) + 1)
            .putLong("t:" + packageName, System.currentTimeMillis()).apply()
    }

    /** T23: packages opened from the launcher, newest first (the time stamp is kept beside the count). */
    fun recent(context: Context, n: Int): List<String> =
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).all
            .mapNotNull { (k, v) -> if (k.startsWith("t:") && v is Long) k.removePrefix("t:") to v else null }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(n)

    fun top(context: Context, n: Int): List<String> =
        context.getSharedPreferences(Prefs, Context.MODE_PRIVATE).all
            .mapNotNull { (k, v) -> (v as? Int)?.let { k to it } }
            .sortedByDescending { it.second }
            .map { it.first }
            .take(n)
}

/** Apps the launcher itself ranks as frequently used (launches through search results). */
internal class CommandLayerVM : ViewModel(), KoinComponent {
    private val favoritesService: FavoritesService by inject()
    val permissionsManager: PermissionsManager by inject()
    val hasContacts = permissionsManager.hasPermission(PermissionGroup.Contacts)
    private val notificationRepository: de.mm20.launcher2.notifications.NotificationRepository by inject()

    /** T18: active notifications; matched to the starred contacts in [PeopleAndSwitches]. */
    val notifications = notificationRepository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val rankedPackages = favoritesService.getFavorites(
        includeTypes = listOf("app"),
        minPinnedLevel = PinnedLevel.FrequentlyUsed,
        maxPinnedLevel = PinnedLevel.FrequentlyUsed,
        limit = 12,
    ).map { list -> list.filterIsInstance<Application>().map { it.componentName.packageName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
}

private fun appLabel(context: Context, packageName: String): String? = try {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
} catch (e: Exception) {
    Log.e(Tag, "No label for $packageName", e)
    null
}

@Composable
private fun MostUsedApps(tick: Int) {
    val context = LocalContext.current
    val vm: CommandLayerVM = viewModel()
    val ranked by vm.rankedPackages.collectAsState()
    val apps = remember(tick, ranked) {
        (UsageCounter.top(context, 12) + ranked)
            .distinct()
            .filter { it != context.packageName && context.packageManager.getLaunchIntentForPackage(it) != null }
            .take(8)
    }
    if (apps.isEmpty()) return
    Column {
        LayerRibbon("Most used")
        Box(Modifier.fillMaxWidth().height(1.dp).background(Brush.horizontalGradient(listOf(Medieval.GoldDark, Color.Transparent))))
        Box(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            apps.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { pkg -> AppCell(pkg, Modifier.weight(1f)) }
                    repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
internal fun AppCell(pkg: String, modifier: Modifier, name: String? = null, onLongClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val label = remember(pkg, name) { name ?: appLabel(context, pkg) ?: "?" }
    Column(
        modifier.combinedClickable(
            onLongClick = onLongClick,
            onClick = {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) launchSafely(context, intent, label)
            },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(44.dp)) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * .5f, h * .04f)
                    lineTo(w * .92f, h * .18f)
                    lineTo(w * .92f, h * .5f)
                    cubicTo(w * .92f, h * .78f, w * .74f, h * .92f, w * .5f, h * .98f)
                    cubicTo(w * .26f, h * .92f, w * .08f, h * .78f, w * .08f, h * .5f)
                    lineTo(w * .08f, h * .18f)
                    close()
                }
                drawPath(path, Medieval.ShieldFill)
                drawPath(path, Medieval.GoldDeep, style = Stroke(1.2.dp.toPx()))
            }
            ShieldEmblem(pkg, label)
        }
        MText(
            label.uppercase(), 8.5f, Medieval.Label, Modifier.padding(horizontal = 2.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.12f, align = TextAlign.Center,
        )
    }
}

// ---- People and switches -------------------------------------------------------------------

/** Screenshot tests put fake contacts here (the test phone has no contacts provider). */
internal var StarredForTests: (() -> List<Person>)? = null

internal class Person(val name: String, val lookupUri: Uri, val photo: Uri?, val lookupKey: String, val phoneDigits: Set<String>)

/** Digits of one contact's phone numbers, for matching notifications (never logged). */
private fun loadPhoneDigits(context: Context, lookupKey: String): Set<String> {
    val out = HashSet<String>()
    try {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY} = ?", arrayOf(lookupKey), null,
        )?.use { c ->
            while (c.moveToNext()) c.getString(0)?.filter { it.isDigit() }?.takeIf { it.isNotEmpty() }?.let { out += it }
        }
    } catch (e: Exception) {
        Log.e(Tag, "Could not read contact numbers", e)
    }
    return out
}

private fun loadStarred(context: Context): List<Person> {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED
    ) return emptyList()
    val out = ArrayList<Person>()
    try {
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
            ),
            "${ContactsContract.Contacts.STARRED} = 1", null,
            "${ContactsContract.Contacts.LAST_TIME_CONTACTED} DESC, ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC",
        )?.use { c ->
            while (c.moveToNext() && out.size < 5) {
                val name = c.getString(2) ?: continue
                val key = c.getString(1) ?: continue
                out += Person(
                    name,
                    ContactsContract.Contacts.getLookupUri(c.getLong(0), key),
                    c.getString(3)?.let { Uri.parse(it) },
                    key,
                    loadPhoneDigits(context, key),
                )
            }
        }
    } catch (e: Exception) {
        Log.e(Tag, "Could not read starred contacts", e)
    }
    return out
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeopleAndSwitches(tick: Int, onChanged: () -> Unit) {
    val context = LocalContext.current
    val vm: CommandLayerVM = viewModel()
    val hasContacts by vm.hasContacts.collectAsState(initial = null)
    val people by produceState(emptyList<Person>(), tick, hasContacts) {
        value = withContext(Dispatchers.IO) { StarredForTests?.invoke() ?: loadStarred(context) }
    }
    val all by vm.notifications.collectAsState()
    val alerts = remember(all, people) {
        contactAlerts(people.map { ContactIdentity(it.name, it.lookupKey, it.phoneDigits) }, all.asDispatches())
    }
    Box(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Medieval.PanelTop, Medieval.PanelBottom)))
                .border(1.dp, Medieval.GoldDark)
                .padding(start = 12.dp, end = 12.dp, top = 22.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (hasContacts == false || (hasContacts == true && people.isEmpty())) {
                MText(
                    if (hasContacts == false) "Tap to allow contacts" else "Star contacts in your Contacts app to see them here",
                    13f, Medieval.TextDim,
                    if (hasContacts == false) Modifier.clickable {
                        (context as? AppCompatActivity)?.let { vm.permissionsManager.requestPermission(it, PermissionGroup.Contacts) }
                    } else Modifier,
                    italic = true, maxLines = 2,
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(Medieval.GoldLine))
            }
            if (people.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    people.forEach { PersonCell(it, alerts[it.lookupKey].orEmpty()) }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Medieval.GoldLine))
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switches(tick, onChanged)
            }
        }
        Box(Modifier.align(Alignment.TopStart).offset(12.dp, (-8).dp)) {
            LayerRibbon(if (people.isNotEmpty() || hasContacts != null) "People and switches" else "Switches")
        }
    }
}

@Composable
private fun PersonCell(p: Person, alerts: List<Notification>) {
    val context = LocalContext.current
    val count = alerts.size
    // T18: the gold halo of a new dispatch (once, 1.8 s) and the badge swell; nothing moves while the gate is closed
    val halo = rememberOneShot(rememberRises(alerts.maxOfOrNull { it.postTime } ?: 0L), 1_800L, LocalMotion.current.ambient)
    val swell = rememberOneShot(rememberRises(count.toLong()), 1_800L, LocalMotion.current.ambient)
    val photo by produceState<ImageBitmap?>(null, p.photo) {
        value = p.photo?.let { uri ->
            withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (e: Exception) {
                    Log.e(Tag, "Could not read a contact photo", e)
                    null
                }
            }
        }
    }
    Column(
        Modifier
            .width(56.dp)
            .clickable {
                // unread messages: the newest one opens, as in Dispatches; else the contact card
                val newest = alerts.maxByOrNull { it.postTime }
                if (newest != null) openDispatch(context, newest)
                // "contact", not the name: launchSafely logs [what], and contact names are never logged
                else launchSafely(context, Intent(Intent.ACTION_VIEW, p.lookupUri), "contact")
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(40.dp).drawBehind {
            val k = bump(halo.value, 0f, 1f)
            if (k <= 0f) return@drawBehind
            val step = 1.5.dp.toPx()
            for (i in 1..8) {
                drawCircle(
                    Medieval.Glow.copy(alpha = 0.45f * k * (1f - i / 9f) / 2f),
                    radius = size.minDimension / 2 + step * i, style = Stroke(step),
                )
            }
        }) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Medieval.palette.avatarA, Medieval.palette.avatarB)))
                    .border(1.5.dp, Medieval.GoldDeep, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val bmp = photo
                if (bmp != null) {
                    Image(bmp, null, Modifier.size(40.dp), contentScale = ContentScale.Crop)
                } else {
                    MText(
                        p.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?", 15f, Medieval.Gold,
                        font = Medieval.Cinzel, bold = true,
                    )
                }
            }
            if (count > 0) Badge(count, swell, Modifier.align(Alignment.TopEnd).offset(8.dp, (-4).dp))
        }
        MText(
            p.name.substringBefore(' '), 11f, Medieval.TextDim,
            font = Medieval.FellEnglish, italic = true, align = TextAlign.Center,
        )
    }
}

// ---- Switches -------------------------------------------------------------------------------

private val WifiPaths = listOf(
    "M3 9c5-5 13-5 18 0M6 12.5c3.5-3.5 8.5-3.5 12 0M9 16c1.7-1.7 4.3-1.7 6 0",
    svgCircle(12f, 19f, 0.8f),
)
private val BluetoothPaths = listOf("M7 7l10 10-5 4V3l5 4L7 17")
private val TorchPaths = listOf(
    "M12 3c2.5 3 4 4.5 4 7a4 4 0 0 1-8 0c0-2.5 1.5-4 4-7z",
    "M9 16h6M10 19h4",
)
private val RotatePaths = listOf(
    "M19 12a7 7 0 0 1-12 4.9M5 12a7 7 0 0 1 12-4.9",
    "M17 3v4h-4M7 21v-4h4",
)
private val SaverPaths = listOf("M5 8h12v10H5z", "M17 11h2.5v4H17", "M10 10l-1.5 3h3L10 16")
private val LocationPaths = listOf(
    "M12 21c-4-4.5-6-7.5-6-10.5a6 6 0 0 1 12 0c0 3-2 6-6 10.5z",
    svgCircle(12f, 10.5f, 2f),
)
private val SoundPaths = listOf("M4 9h4l5-4v14l-5-4H4z", "M16.5 9a4 4 0 0 1 0 6")
private val VibratePaths = listOf("M4 9h4l5-4v14l-5-4H4z", "M16 8l1.5 2-1.5 2 1.5 2-1.5 2", "M20 9l1 1.5-1 1.5 1 1.5-1 1.5")
private val MutePaths = listOf("M4 9h4l5-4v14l-5-4H4z", "M17 9l5 6M22 9l-5 6")

@Composable
private fun Switches(tick: Int, onChanged: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val main = remember { Handler(Looper.getMainLooper()) }

    fun hint(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    fun open(intent: Intent, what: String) = launchSafely(context, intent, what)

    // Wi-Fi: opens the system sheet
    val wifiOn = remember(tick) {
        try {
            (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).isWifiEnabled
        } catch (e: Exception) { Log.e(Tag, "Layer: Wi-Fi state", e); false }
    }
    SwitchChip("Wi-Fi", WifiPaths, wifiOn) {
        open(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY), "Wi-Fi")
    }

    val btOn = remember(tick) {
        try {
            Settings.Global.getInt(context.contentResolver, "bluetooth_on", 0) == 1
        } catch (e: Exception) { Log.e(Tag, "Layer: Bluetooth state", e); false }
    }
    SwitchChip("Bluetooth", BluetoothPaths, btOn) {
        open(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Bluetooth")
    }

    // Flashlight: real toggle (no permission). State comes from a torch callback.
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val torchId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull {
                val ch = cameraManager.getCameraCharacteristics(it)
                ch.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    ch.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }
        } catch (e: Exception) { Log.e(Tag, "Layer: no torch found", e); null }
    }
    var torchOn by remember { mutableStateOf(false) }
    DisposableEffect(cameraManager) {
        val cb = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == torchId) torchOn = enabled
            }
        }
        try {
            cameraManager.registerTorchCallback(cb, main)
        } catch (e: Exception) { Log.e(Tag, "Layer: torch callback", e) }
        onDispose { cameraManager.unregisterTorchCallback(cb) }
    }
    if (torchId != null) {
        SwitchChip("Flashlight", TorchPaths, torchOn, toggles = true) {
            try {
                cameraManager.setTorchMode(torchId, !torchOn)
            } catch (e: Exception) {
                Log.e(Tag, "Layer: could not switch the torch", e)
                hint("The flashlight would not switch")
            }
        }
    }

    // Auto-rotate: real toggle once "Modify system settings" is allowed for this app
    val rotateOn = remember(tick) {
        try {
            Settings.System.getInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1
        } catch (e: Exception) { Log.e(Tag, "Layer: rotation state", e); false }
    }
    SwitchChip("Auto-rotate", RotatePaths, rotateOn, toggles = true) {
        if (!Settings.System.canWrite(context)) {
            hint("Allow \"Modify system settings\" for Deliverance, then come back")
            open(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:${context.packageName}")),
                "the settings page",
            )
        } else {
            try {
                Settings.System.putInt(
                    context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (rotateOn) 0 else 1,
                )
                onChanged()
            } catch (e: Exception) {
                Log.e(Tag, "Layer: could not switch auto-rotate", e)
                hint("Auto-rotate would not switch")
            }
        }
    }

    val saverOn = remember(tick) {
        try {
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode
        } catch (e: Exception) { Log.e(Tag, "Layer: power saving state", e); false }
    }
    SwitchChip("Power saving", SaverPaths, saverOn) {
        open(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS), "Power saving")
    }

    val locationOn = remember(tick) {
        try {
            (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isLocationEnabled
        } catch (e: Exception) { Log.e(Tag, "Layer: location state", e); false }
    }
    SwitchChip("Location", LocationPaths, locationOn) {
        open(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), "Location")
    }

    // Sound: Sound <-> Vibrate (and Mute only if Do Not Disturb access is already granted)
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val nm = remember { context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    val ringer = remember(tick) { audio.ringerMode }
    val soundLabel = when (ringer) {
        AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
        AudioManager.RINGER_MODE_SILENT -> "Mute"
        else -> "Sound"
    }
    val soundPaths = when (ringer) {
        AudioManager.RINGER_MODE_VIBRATE -> VibratePaths
        AudioManager.RINGER_MODE_SILENT -> MutePaths
        else -> SoundPaths
    }
    SwitchChip(soundLabel, soundPaths, ringer == AudioManager.RINGER_MODE_NORMAL, toggles = true) {
        val next = when (ringer) {
            AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
            AudioManager.RINGER_MODE_VIBRATE ->
                if (nm.isNotificationPolicyAccessGranted) AudioManager.RINGER_MODE_SILENT
                else AudioManager.RINGER_MODE_NORMAL
            else -> AudioManager.RINGER_MODE_NORMAL
        }
        try {
            audio.ringerMode = next
            onChanged()
        } catch (e: Exception) {
            Log.e(Tag, "Layer: could not change the ringer mode", e)
            hint("The phone would not change its sound mode")
        }
    }
}

/** One quick-setting chip. [on] brightens it; [toggles] adds the little state dot of a real switch. */
@Composable
private fun SwitchChip(
    label: String,
    icon: List<String>,
    on: Boolean,
    toggles: Boolean = false,
    onClick: () -> Unit,
) {
    val shape = LocalCornerStyle.current.chipShape()
    Row(
        Modifier
            .height(32.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Medieval.PanelTop, Medieval.PanelBottom)))
            .border(1.dp, if (on) Medieval.GoldDeep else Medieval.GoldDark, shape)
            .clickable(onClick = onClick)
            .padding(start = 9.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SvgIcon(icon, 16f, if (on) Medieval.Gold else Medieval.GoldDeep)
        Box(Modifier.width(7.dp))
        MText(
            label.uppercase(), 8.5f, if (on) Medieval.TextSoft else Medieval.TextDim,
            font = Medieval.Cinzel, bold = true, spacingEm = 0.14f,
        )
        if (toggles) {
            Box(Modifier.width(7.dp))
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .then(
                        if (on) Modifier.background(Medieval.Gold)
                        else Modifier.border(1.dp, Medieval.GoldDeep, CircleShape)
                    ),
            )
        }
    }
}

// ---- Ribbon heading -------------------------------------------------------------------------

private class LayerRibbonShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val notch = 7f * density.density
        return Outline.Generic(Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - notch, size.height / 2f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        })
    }
}

/** Red ribbon heading like the Home panels. */
@Composable
private fun LayerRibbon(text: String) {
    Box(
        Modifier
            .height(17.dp)
            .clip(LayerRibbonShape())
            .background(Brush.verticalGradient(listOf(Medieval.Oxblood, Medieval.OxbloodDark)))
            .drawBehind { drawLine(Medieval.Gold, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 11.dp, end = 17.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        MText(
            text.uppercase(), 8.5f, Medieval.RibbonText,
            font = Medieval.Cinzel, bold = true, spacingEm = 0.24f,
        )
    }
}
