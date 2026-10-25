package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private const val PrefsName = "medieval_home"

/** One heater shield: where it sits, its default app and its C5 line glyph. */
private class ShieldSlot(
    val index: Int,
    val x: Float,
    val y: Float,
    val labelX: Float,
    val labelY: Float,
    val defaultPackage: String,
    val glyph: List<String>,
)

private val Slots = listOf(
    ShieldSlot(0, 24f, 276f, 13f, 352f, "com.twitter.android", MedievalIcons.Bird),
    ShieldSlot(1, 304f, 276f, 293f, 352f, "com.discord", MedievalIcons.Mug),
    ShieldSlot(2, 24f, 372f, 13f, 448f, "com.google.android.youtube", MedievalIcons.Play),
    ShieldSlot(3, 304f, 372f, 293f, 448f, "com.samsung.android.messaging", MedievalIcons.Letter),
)

internal class AppEntry(val packageName: String, val label: String)

private fun isInstalled(context: Context, packageName: String): Boolean =
    context.packageManager.getLaunchIntentForPackage(packageName) != null

/** The package of a slot: the owner's pick, else the default if it is installed, else null. */
private fun slotPackage(context: Context, slot: ShieldSlot): String? {
    val picked = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
        .getString("shield_${slot.index}", null)
    if (picked != null && isInstalled(context, picked)) return picked
    return slot.defaultPackage.takeIf { isInstalled(context, it) }
}

private fun appLabel(context: Context, packageName: String): String? = try {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
} catch (e: Exception) {
    Log.e("medieval", "No label for $packageName", e)
    null
}

private fun launchableApps(context: Context): List<AppEntry> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { AppEntry(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

internal fun drawableToBitmap(drawable: Drawable, size: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
}

@Composable
private fun ShieldBody() {
    Canvas(Modifier.size(62.dp, 72.dp)) {
        val shield = Path().apply {
            moveTo(31f, 2f); lineTo(58f, 12f); lineTo(58f, 38f)
            cubicTo(58f, 55f, 44f, 66f, 31f, 70f)
            cubicTo(18f, 66f, 4f, 55f, 4f, 38f)
            lineTo(4f, 12f); close()
        }
        val top = Path().apply {
            moveTo(4f, 12f); lineTo(31f, 2f); lineTo(58f, 12f); lineTo(58f, 22f); lineTo(4f, 22f); close()
        }
        val k = size.width / 62f
        drawContext.canvas.save()
        drawContext.canvas.scale(k, k)
        drawPath(shield, Medieval.ShieldFill)
        drawPath(top, Medieval.ShieldTop)
        drawPath(shield, Medieval.GoldDeep, style = Stroke(2f))
        drawContext.canvas.restore()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Shield(slot: ShieldSlot, refresh: Int, onPick: () -> Unit) {
    val context = LocalContext.current
    val pkg = remember(refresh) { slotPackage(context, slot) }
    val label = remember(pkg) { pkg?.let { appLabel(context, it) } ?: "Empty" }
    val icon = remember(pkg) {
        if (pkg == null || pkg == slot.defaultPackage) null else try {
            drawableToBitmap(context.packageManager.getApplicationIcon(pkg), 96).asImageBitmap()
        } catch (e: Exception) {
            Log.e("medieval", "No icon for $pkg", e)
            null
        }
    }

    Box(
        Modifier
            .at(slot.x, slot.y)
            .size(62.dp, 72.dp)
            .combinedClickable(
                onClick = {
                    if (pkg == null) {
                        onPick()
                    } else {
                        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                        if (intent != null) launchSafely(context, intent, label) else onPick()
                    }
                },
                onLongClick = onPick,
            )
    ) {
        ShieldBody()
        // Badges stay hidden until notification access exists (T3).
        Box(Modifier.fillMaxWidth().padding(top = 22.dp), contentAlignment = Alignment.TopCenter) {
            when {
                icon != null -> Image(icon, null, Modifier.size(26.dp))
                pkg == null -> MText("+", 22f, Medieval.TextFaint)
                else -> SvgIcon(slot.glyph, 26f, Medieval.Text)
            }
        }
    }
    MText(
        label.uppercase(), 10f, Color(0xFFBFAE86),
        Modifier.at(slot.labelX, slot.labelY).width(84.dp),
        font = Medieval.Cinzel, bold = true, spacingEm = 0.18f, align = TextAlign.Center,
    )
}

/** Four heater shields around the orb. Tap opens the app, long-press (or tap on empty) picks one. */
@Composable
internal fun Shields() {
    val context = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    var picking by remember { mutableStateOf<ShieldSlot?>(null) }

    for (slot in Slots) {
        Shield(slot, refresh) { picking = slot }
    }

    val apps = remember { launchableApps(context) }
    val slot = picking
    if (slot != null) {
        AppPickerDialog(
            apps = apps,
            onDismiss = { picking = null },
            onPick = { entry ->
                context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit()
                    .putString("shield_${slot.index}", entry.packageName).apply()
                refresh++
                picking = null
            },
        )
    }
}
