package de.mm20.launcher2.ui.medieval

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.mm20.launcher2.notifications.Notification

private val PillGold = Color(0xFFE0B25C)
private const val MaxNotificationIcons = 4

/** The camera hole in window pixels (centre and size). */
private class Hole(val cx: Float, val cy: Float, val w: Float, val h: Float)

/** Top cutout of the window; falls back to the top centre (about 14 dp down, 12 dp wide). */
private fun findHole(view: android.view.View, density: Float, statusBarPx: Int): Hole {
    val rects: List<Rect> = ViewCompat.getRootWindowInsets(view)?.displayCutout?.boundingRects.orEmpty()
    val top = rects.filter { it.top <= 8 * density }.minByOrNull { it.top }
    if (top == null) {
        Log.w("medieval", "Pill: no display cutout, using top centre of the status bar (height ${statusBarPx}px)")
        return Hole(view.width / 2f, if (statusBarPx > 0) statusBarPx / 2f else 14f * density, 12f * density, 12f * density)
    }
    // The cutout rect is a bounding box that may be taller than the hole, so its centre sat above the
    // hole on the phone. The cutout path (API 31+) is the hole's real outline: centre on its bounds.
    var shape = RectF(top)
    if (Build.VERSION.SDK_INT >= 31) {
        val path = view.rootWindowInsets?.displayCutout?.cutoutPath
        if (path != null) {
            val b = RectF()
            path.computeBounds(b, true)
            if (!b.isEmpty) shape = b
        }
    }
    Log.i("medieval", "Pill: cutout rect $top, hole shape $shape, status bar ${statusBarPx}px")
    return Hole(shape.centerX(), shape.centerY(), shape.width(), shape.height())
}

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** Hides the system top bar (the pill replaces it) while the Home screen is shown and the launcher is resumed. */
@Composable
internal fun HideStatusBarWhileHome() {
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(view, lifecycleOwner) {
        val activity = view.context.findActivity()
        if (activity == null) {
            Log.e("medieval", "Pill: no activity, status bar stays")
            return@DisposableEffect onDispose {}
        }
        val controller = WindowInsetsControllerCompat(activity.window, view)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) controller.hide(WindowInsetsCompat.Type.statusBars())
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }
}

/**
 * The camera pill (design T5-pill v5): a small black capsule that hugs the camera hole.
 * Left of the hole: icons of waiting notifications (+n). Right: Hushed, Bluetooth, Wi-Fi, signal,
 * each only while it applies. The pill grows and shrinks with its icons; the hole stays put.
 * Has no touch handling, so touches pass through.
 */
@Composable
internal fun MedievalPill(notifications: List<Notification>, hasNotificationAccess: Boolean) {
    val status = rememberPillStatus()
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val statusBarPx = WindowInsets.statusBarsIgnoringVisibility.getTop(density)
    // read again whenever the pill is laid out: the window insets are not there on the first frame
    var hole by remember { mutableStateOf(Hole(0f, 14f * density.density, 12f * density.density, 12f * density.density)) }
    var originX by remember { mutableFloatStateOf(0f) }
    var originY by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val p = it.positionInWindow()
                originX = p.x
                originY = p.y
                val h = findHole(view, density.density, statusBarPx)
                if (h.cx != hole.cx || h.cy != hole.cy || h.w != hole.w || h.h != hole.h) hole = h
            }
    ) {
        val holeDp = with(density) { minOf(hole.w, hole.h).toDp() }
        val pillHeight = maxOf(26.dp, holeDp + 10.dp)
        val topPx = hole.cy - originY - with(density) { pillHeight.toPx() } / 2f
        Pill(
            holeCenterX = hole.cx - originX,
            holeSize = holeDp,
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight)
                .offset { IntOffset(0, topPx.toInt()) },
            left = { NotificationIcons(notifications, hasNotificationAccess) },
            right = { StatusIcons(status) },
        )
    }
}

@Composable
private fun Pill(
    holeCenterX: Float,
    holeSize: Dp,
    modifier: Modifier,
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box(
                Modifier
                    .layoutId("bg")
                    .background(Color(0xFF050505), CircleShape)
                    .border(1.dp, Medieval.GoldDark, CircleShape)
            )
            Box(
                Modifier
                    .layoutId("hole")
                    .size(holeSize)
                    .background(Color(0xFF0C0D10), CircleShape)
                    .border(1.5.dp, Color(0xFF2A2418), CircleShape)
            )
            Box(Modifier.layoutId("left"), contentAlignment = Alignment.CenterStart) { left() }
            Box(Modifier.layoutId("right"), contentAlignment = Alignment.CenterEnd) { right() }
        },
    ) { measurables, constraints ->
        val loose = Constraints()
        fun find(id: String) = measurables.first { it.layoutId == id }
        val l = find("left").measure(loose)
        val r = find("right").measure(loose)
        val hole = find("hole").measure(loose)
        val pad = 11.dp.roundToPx()
        val gap = 7.dp.roundToPx()
        val leftPart = if (l.width > 0) l.width + gap else 0
        val rightPart = if (r.width > 0) gap + r.width else 0
        val width = pad + leftPart + hole.width + rightPart + pad
        val height = constraints.maxHeight
        val bg = find("bg").measure(Constraints.fixed(width, height))
        val x = (holeCenterX - (pad + leftPart + hole.width / 2f)).toInt()
        layout(constraints.maxWidth, height) {
            bg.place(x, 0)
            l.place(x + pad, (height - l.height) / 2)
            hole.place(x + pad + leftPart, (height - hole.height) / 2)
            r.place(x + pad + leftPart + hole.width + gap, (height - r.height) / 2)
        }
    }
}

@Composable
private fun NotificationIcons(notifications: List<Notification>, hasAccess: Boolean) {
    val context = LocalContext.current
    // newest first, one icon per app, like the system status bar
    val apps = remember(notifications, hasAccess) {
        if (!hasAccess) emptyList()
        else notifications
            .filter { it.isClearable }
            .sortedByDescending { it.postTime }
            .distinctBy { it.packageName }
    }
    if (apps.isEmpty()) return
    val shown = apps.take(MaxNotificationIcons)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        shown.forEach { n ->
            val bitmap = remember(n.key, n.postTime) {
                try {
                    n.smallIcon?.loadDrawable(context)?.let { drawableToBitmap(it, 40).asImageBitmap() }
                } catch (e: Exception) {
                    Log.e("medieval", "Pill: no small icon for ${n.packageName}", e)
                    null
                }
            }
            if (bitmap != null) {
                Image(bitmap, null, Modifier.size(11.dp), colorFilter = ColorFilter.tint(PillGold))
            } else {
                Box(Modifier.size(5.dp).background(PillGold, CircleShape))
            }
        }
        if (apps.size > shown.size) {
            Cap("+${apps.size - shown.size}", size = 8f, color = Medieval.Cap, spacingEm = 0.1f)
        }
    }
}

@Composable
private fun StatusIcons(status: PillStatus) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        if (status.hushed) SvgIcon(MedievalIcons.Moon, 11f, PillGold, strokeWidth = 2.2f)
        if (status.bluetooth) SvgIcon(MedievalIcons.Bluetooth, 11f, PillGold, strokeWidth = 2.2f)
        if (status.wifi) SvgIcon(MedievalIcons.Wifi, 12f, PillGold, strokeWidth = 2.2f)
        if (status.signal >= 0) SignalBars(status.signal)
    }
}

/** Four rising bars; [level] of them lit. */
@Composable
private fun SignalBars(level: Int) {
    Canvas(Modifier.size(11.dp)) {
        val bar = size.width / 7f
        for (i in 0 until 4) {
            val h = size.height * (0.3f + 0.7f * i / 3f)
            drawRoundRect(
                color = PillGold.copy(alpha = if (i < level) 1f else 0.3f),
                topLeft = Offset(i * (bar * 2f), size.height - h),
                size = Size(bar * 1.4f, h),
                cornerRadius = CornerRadius(bar * 0.3f),
            )
        }
    }
}
