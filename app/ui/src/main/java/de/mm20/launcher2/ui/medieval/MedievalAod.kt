package de.mm20.launcher2.ui.medieval

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import kotlinx.serialization.Serializable
import java.time.LocalDate

/*
 * T21 "Always-on display pictures": the eight A1 pictures ship in the APK (assets/aod/) and can be saved to
 * Pictures/Deliverance. Samsung has no public way for an app to pick the Image clock picture, so the owner
 * chooses it once in Samsung's menu; this page makes that short. No permission, no background work.
 */

@Serializable
data object AodSettingsRoute : NavKey

private const val Tag = "medieval"
private const val Folder = "Deliverance"
private const val PicturesPath = "Pictures/$Folder"

/** The eight picture names: the seven weekdays by planet, and the plain ring picture that works any day. */
private val AodNames = Planet.entries.map { it.label.lowercase() } + "plain"

private fun assetPath(name: String) = "aod/aod-imageclock-$name-1200x900.png"

private fun todayName() = planetOf(LocalDate.now().dayOfWeek).label.lowercase()

/** Result of one save: how many pictures were written, and how many were already in the folder. */
internal data class AodSaveResult(val saved: Int, val already: Int, val failed: Int, val tooOld: Boolean = false)

/** Writes the named pictures into Pictures/Deliverance with MediaStore. Android 10+ needs no permission for this. */
internal fun saveAodPictures(context: Context, names: List<String>): AodSaveResult {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Log.e(Tag, "AOD save: Android ${Build.VERSION.SDK_INT} is older than 10, MediaStore folder save needs a permission there")
        return AodSaveResult(0, 0, names.size, tooOld = true)
    }
    val resolver = context.contentResolver
    var saved = 0
    var already = 0
    var failed = 0
    for (name in names) {
        val file = "deliverance-aod-$name.png"
        try {
            val exists = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.DISPLAY_NAME} = ? AND ${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?",
                arrayOf(file, "$PicturesPath%"),
                null,
            )?.use { it.moveToFirst() } ?: false
            if (exists) {
                already++
                continue
            }
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, PicturesPath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri == null) {
                Log.e(Tag, "AOD save: MediaStore insert returned null for $file")
                failed++
                continue
            }
            try {
                val out = resolver.openOutputStream(uri) ?: error("no output stream for $uri")
                out.use { o -> context.assets.open(assetPath(name)).use { it.copyTo(o) } }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                saved++
            } catch (e: Exception) {
                Log.e(Tag, "AOD save: writing $file failed, removing the half-written entry", e)
                resolver.delete(uri, null, null)
                failed++
            }
        } catch (e: Exception) {
            Log.e(Tag, "AOD save: $file failed", e)
            failed++
        }
    }
    Log.i(Tag, "AOD save: saved=$saved already=$already failed=$failed")
    return AodSaveResult(saved, already, failed)
}

private fun AodSaveResult.message(count: Int): String = when {
    tooOld -> "This Android version is too old for saving without a permission."
    failed > 0 -> "Could not save $failed of $count. Nothing else was changed."
    saved == 0 -> "Already in Pictures/$Folder"
    else -> "Saved to $PicturesPath"
}

/** Opens Samsung's display settings (the screen that holds Always On Display on One UI); falls back to Settings. */
internal fun openAodSettings(context: Context): String {
    val display = Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(display)
        Log.i(Tag, "AOD settings: opened ACTION_DISPLAY_SETTINGS")
        return "display"
    } catch (e: Exception) {
        Log.e(Tag, "AOD settings: ACTION_DISPLAY_SETTINGS failed, using the main Settings screen", e)
    }
    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    Log.i(Tag, "AOD settings: opened ACTION_SETTINGS")
    return "settings"
}

/** The "Always-on display pictures" row on the main settings page (opens [AodSettingsScreen]). */
@Composable
fun MedievalAodPreference(onClick: () -> Unit) {
    Preference(
        title = "Lock screen and always-on display",
        summary = "Set the orb as your lock screen, save the always-on pictures",
        onClick = onClick,
    )
}

@Composable
fun AodSettingsScreen() {
    val context = LocalContext.current
    var status by remember { mutableStateOf<String?>(null) }
    var lockStatus by remember { mutableStateOf<String?>(null) }
    var canRestore by remember { mutableStateOf(hasLockBackup(context)) }
    val today = remember { todayName() }
    AodSettingsContent(
        today = today,
        status = status,
        onSaveToday = { status = saveAodPictures(context, listOf(today)).message(1) },
        onSaveAll = { status = saveAodPictures(context, AodNames).message(AodNames.size) },
        onOpenSettings = { openAodSettings(context) },
        lockStatus = lockStatus,
        canRestore = canRestore,
        onSetLock = {
            lockStatus = setOrbAsLockWallpaper(context).message
            canRestore = hasLockBackup(context)
        },
        onRestoreLock = {
            lockStatus = restoreLockWallpaper(context).message
            canRestore = hasLockBackup(context)
        },
    )
}

@Composable
internal fun AodSettingsContent(
    today: String,
    status: String?,
    onSaveToday: () -> Unit,
    onSaveAll: () -> Unit,
    onOpenSettings: () -> Unit,
    lockStatus: String? = null,
    canRestore: Boolean = false,
    onSetLock: () -> Unit = {},
    onRestoreLock: () -> Unit = {},
) {
    PreferenceScreen(title = "Lock screen and always-on display") {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AodCard {
                    Text("The lock screen picture", style = MaterialTheme.typography.titleSmall, color = Medieval.Gold)
                    Text(
                        "Puts the orb on your lock screen only. Your home screen is not touched. The orb sits low, so the clock stays clear.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Medieval.Text,
                    )
                    LockPreview()
                    Button(onClick = onSetLock, modifier = Modifier.fillMaxWidth()) { Text("Set the orb as my lock screen") }
                    if (canRestore) {
                        OutlinedButton(onClick = onRestoreLock, modifier = Modifier.fillMaxWidth()) { Text("Put my old wallpaper back") }
                    }
                    Text(
                        lockStatus ?: "Nothing has been changed yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (lockStatus != null) Medieval.Gold else Medieval.TextDim,
                    )
                }
                AodCard {
                    Text(
                        "Today is ${today.replaceFirstChar { it.uppercase() }}'s day. This is today's picture:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Medieval.Text,
                    )
                    AodPreview(today)
                    Button(onClick = onSaveToday, modifier = Modifier.fillMaxWidth()) { Text("Save today's picture to Gallery") }
                    OutlinedButton(onClick = onSaveAll, modifier = Modifier.fillMaxWidth()) { Text("Save all eight") }
                    Text(
                        status ?: "The pictures go into the folder Pictures/$Folder. No permission is needed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (status != null) Medieval.Gold else Medieval.TextDim,
                    )
                }
                AodCard {
                    Text("Choose it once, in 5 steps", style = MaterialTheme.typography.titleSmall, color = Medieval.Gold)
                    listOf(
                        "Tap the button below. Samsung's settings open.",
                        "Go to Lock screen, then Always On Display.",
                        "Tap Clock style, then Image clock.",
                        "Tap Gallery and pick the picture from the album $Folder.",
                        "Move and crop it if you like, then tap Done.",
                    ).forEachIndexed { i, s ->
                        Text("${i + 1}. $s", style = MaterialTheme.typography.bodyMedium, color = Medieval.Text)
                    }
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) { Text("Open Always On Display settings") }
                }
                AodCard {
                    Text(
                        "This page was called Always-on display pictures before. Samsung does not let apps change the always-on picture, so you pick the picture yourself, once. " +
                            "It does not change by itself each day: to follow the weekday, pick the new day's picture by hand. " +
                            "Or never change it: the plain ring picture looks right on every day. " +
                            "This page runs nothing in the background and uses no battery.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Medieval.TextDim,
                    )
                }
            }
        }
    }
}

/** Sharp panel with the double gold hairline, in the live palette. */
@Composable
private fun AodCard(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Medieval.PanelTop)
            .border(1.dp, Medieval.GoldDark)
            .padding(4.dp)
            .border(1.dp, Medieval.GoldLine)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) { content() }
}

/** The picture in a small gold frame, 4:3 like the file. */
@Composable
private fun AodPreview(name: String) {
    val context = LocalContext.current
    val bitmap = remember(name) {
        try {
            context.assets.open(assetPath(name)).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (e: Exception) {
            Log.e(Tag, "AOD preview: cannot read ${assetPath(name)}", e)
            null
        }
    }
    Column(
        Modifier
            .fillMaxWidth(0.7f)
            .background(Medieval.Ink)
            .border(2.dp, Medieval.Gold)
            .padding(3.dp)
            .border(1.dp, Medieval.GoldDark)
            .aspectRatio(4f / 3f),
    ) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = "Today's always-on display picture", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth())
        } else {
            Text("Picture missing", color = Medieval.TextDim, modifier = Modifier.padding(8.dp))
        }
    }
}

/** Small preview of the lock screen picture (9:20 like the phone), in a gold frame. */
@Composable
private fun LockPreview() {
    val context = LocalContext.current
    val bitmap = remember {
        try {
            context.assets.open(LockAsset).use { BitmapFactory.decodeStream(it) }?.asImageBitmap()
        } catch (e: Exception) {
            Log.e(Tag, "Lock preview: cannot read $LockAsset", e)
            null
        }
    }
    Column(
        Modifier
            .fillMaxWidth(0.35f)
            .background(Medieval.Ink)
            .border(2.dp, Medieval.Gold)
            .padding(3.dp)
            .border(1.dp, Medieval.GoldDark)
            .aspectRatio(9f / 20f),
    ) {
        if (bitmap != null) {
            Image(bitmap, contentDescription = "Lock screen picture", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth())
        } else {
            Text("Picture missing", color = Medieval.TextDim, modifier = Modifier.padding(8.dp))
        }
    }
}
