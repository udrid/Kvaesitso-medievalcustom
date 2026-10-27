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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.zIndex
import de.mm20.launcher2.notifications.Notification
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private const val PrefsName = "medieval_home"

/** One heater shield: where it sits and its default app. */
private class ShieldSlot(
    val index: Int,
    val x: Float,
    val y: Float,
    val labelX: Float,
    val labelY: Float,
    val defaultPackages: List<String>,
)

private val Slots = listOf(
    ShieldSlot(0, 18f, 136f, 7f, 212f, listOf("com.twitter.android")),
    ShieldSlot(1, 281f, 136f, 270f, 212f, listOf("com.discord")),
    ShieldSlot(2, 18f, 232f, 7f, 308f, listOf("com.google.android.youtube")),
    ShieldSlot(3, 281f, 232f, 270f, 308f, listOf("com.google.android.apps.messaging", "com.samsung.android.messaging")),
)

internal class AppEntry(val packageName: String, val label: String)

private fun isInstalled(context: Context, packageName: String): Boolean =
    context.packageManager.getLaunchIntentForPackage(packageName) != null

/** The package of a slot: the owner's pick, else the default if it is installed, else null. */
private fun slotPackage(context: Context, slot: ShieldSlot): String? {
    val picked = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
        .getString("shield_${slot.index}", null)
    if (picked != null && isInstalled(context, picked)) return picked
    return slot.defaultPackages.firstOrNull { isInstalled(context, it) }
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

/** Oxblood count badge with a gold rim (".badge" in C5.html). */
@Composable
private fun Badge(count: Int, swell: State<Float>, modifier: Modifier = Modifier) {
    // T9: when the count goes up, the badge swells once (two beats, 1.8 s); [swell] is that one-shot
    Box(
        modifier
            .graphicsLayer {
                val p = swell.value
                val s = 1f + 0.35f * bump(p, 0f, 0.55f) + 0.15f * bump(p, 0.55f, 1f)
                scaleX = s
                scaleY = s
            }
            .defaultMinSize(minWidth = 18.dp)
            .height(18.dp)
            .zIndex(2f)
            .background(Color(0xFF6E1A14), CircleShape)
            .border(1.5.dp, Medieval.Gold, CircleShape)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        MText(
            count.toString(), 10f, Color(0xFFF2E6C8),
            font = Medieval.Cinzel, bold = true,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Shield(slot: ShieldSlot, refresh: Int, notifications: List<Notification>, onPick: () -> Unit) {
    val context = LocalContext.current
    val pkg = remember(refresh) { slotPackage(context, slot) }
    val label = remember(pkg) { pkg?.let { appLabel(context, it) } ?: "Empty" }

    val tap = rememberTapReaction()
    val count = if (pkg == null) 0 else notifications.count { it.packageName == pkg }
    // tracked here, not in the badge, so the first notification (badge appearing) also swells
    val swell = rememberOneShot(rememberRises(count.toLong()), 1_800L, LocalMotion.current.ambient)
    Box(
        Modifier
            .at(slot.x, slot.y)
            .size(62.dp, 72.dp)
            .tapReaction(tap)
            .combinedClickable(
                interactionSource = tap.source,
                indication = null,
                onClick = {
                    if (pkg == null) {
                        onPick()
                    } else {
                        tap.open {
                            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                            if (intent != null) launchSafely(context, intent, label) else onPick()
                        }
                    }
                },
                onLongClick = onPick,
            )
    ) {
        ShieldBody()
        if (count > 0) Badge(count, swell, Modifier.align(Alignment.TopEnd).offset(3.dp, (-5).dp))
        Box(Modifier.fillMaxWidth().padding(top = 22.dp), contentAlignment = Alignment.TopCenter) {
            when {
                pkg == null -> MText("+", 22f, Medieval.TextFaint)
                else -> ShieldEmblem(pkg, label)
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
internal fun Shields(vm: MedievalHomeVM) {
    val context = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    var picking by remember { mutableStateOf<ShieldSlot?>(null) }
    val all by vm.notifications.collectAsState()
    val dispatches = remember(all) { all.asDispatches() }

    for (slot in Slots) {
        Shield(slot, refresh, dispatches) { picking = slot }
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
