package de.mm20.launcher2.ui.medieval

import android.util.Log
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView

/*
 * S1 Home in landscape (owner picks 2026-10-27: layout A three columns; S6 owner pick 2026-10-28: option D, taller columns and a Forecast strip, standing pill P1, dock as a row).
 * Portrait is laid out on a 390 x 842 stage, landscape on a 760 x 374 stage (docs/design/S1-landscape.html).
 * Every section has a rectangle in each stage; on a turn the new layout is drawn at once and each section
 * glides (position, size, fade) from where it was in the old layout to its new place, 350 ms, once.
 */

/** True while the landscape stage is drawn. */
internal val LocalLandscape = compositionLocalOf { false }

/** Width in stage units of the section being drawn: 361 (the portrait body), or its landscape column. */
internal val LocalColumnWidth = compositionLocalOf { 361f }

internal const val LandscapeStageWidth = 760f
internal const val LandscapeStageHeight = 374f
private const val GlideMs = 350

internal enum class Sec { Header, Treasury, Pennants, Oracle, Ledger, Command, Dispatches, Dock, Forecast, Recent }

internal class SRect(val x: Float, val y: Float, val w: Float, val h: Float)

/** Where each section sits on the portrait stage (body units * 1.08 + body top). */
private val PortraitRects = mapOf(
    Sec.Header to SRect(22f, 30f, 346f, 94f),
    Sec.Treasury to SRect(17f, 142f, 355f, 60f),
    Sec.Pennants to SRect(15f, 211f, 358f, 67f),
    Sec.Oracle to SRect(0f, 269f, 390f, 220f),
    Sec.Ledger to SRect(17f, 498f, 355f, 73f),
    Sec.Command to SRect(17f, 578f, 355f, 48f),
    Sec.Dispatches to SRect(17f, 632f, 355f, 105f),
    Sec.Dock to SRect(0f, 745f, 390f, 90f),
    // S6: the Forecast strip is landscape only; in portrait it sits (invisible) at the orb so it grows out of it on a turn
    Sec.Forecast to SRect(95f, 380f, 200f, 20f),
    Sec.Recent to SRect(17f, 600f, 200f, 20f),
)

/**
 * Landscape layout D (S6): left column header, Treasury, pennants; middle orb, shields, Forecast strip and the
 * dock; right column Ledger (4 events), command bar, Dispatches (5 rows). The side columns are 232 wide: their
 * sections are laid out 222 wide (Treasury 205) and drawn up to fill it (see Place in MedievalHomeComponent).
 */
internal val LandscapeRects = mapOf(
    Sec.Header to SRect(0f, 0f, 232f, 150f),
    Sec.Treasury to SRect(0f, 154f, 232f, 75f),
    Sec.Pennants to SRect(0f, 233f, 232f, 80f),
    Sec.Oracle to SRect(232f, 14f, 296f, 190f),
    Sec.Forecast to SRect(240f, 212f, 280f, 58f),
    Sec.Ledger to SRect(528f, 8f, 232f, 132f),
    Sec.Command to SRect(528f, 148f, 232f, 46f),
    Sec.Dispatches to SRect(528f, 208f, 232f, 150f),
    Sec.Dock to SRect(232f, 284f, 296f, 88f),
    // T23: recently used apps under the pennants, down to the bottom of the stage (372)
    Sec.Recent to SRect(0f, 310f, 232f, RecentPanelHeight),
)

internal fun rectsOf(landscape: Boolean) = if (landscape) LandscapeRects else PortraitRects

/** Stage position and scale inside the window, in dp (the stage is drawn centred in the free area and scaled). */
internal class StageGeo(val landscape: Boolean, val scale: Float, val x: Float, val y: Float) {
    /** A stage rectangle in window dp. */
    fun toWindow(r: SRect) = SRect(x + r.x * scale, y + r.y * scale, r.w * scale, r.h * scale)
}

/** [free] is the area (dp) left after the bars and the camera strip: left, top, right, bottom edges inside the window. */
internal fun stageGeo(landscape: Boolean, left: Float, top: Float, right: Float, bottom: Float): StageGeo {
    val w = right - left
    val h = bottom - top
    val sw = if (landscape) LandscapeStageWidth else StageWidth
    val sh = if (landscape) LandscapeStageHeight else StageHeight
    val s = minOf(w / sw, h / sh)
    return StageGeo(landscape, s, left + (w - sw * s) / 2f, top + (h - sh * s) / 2f)
}

/**
 * Drives the turn. [progress] runs 0 to 1 over 350 ms when [landscape] changes (not on the first composition);
 * snaps to 1 when the motion gate is closed (Animations Off, Battery Saver, Android's remove-animations, ...).
 */
internal class RotateGlide(
    val progress: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    private val from: StageGeo,
    private val to: StageGeo,
) {
    /** The section's rectangle in the old layout, in the new stage's units. */
    private fun fromRect(sec: Sec): SRect {
        val w = from.toWindow(rectsOf(from.landscape).getValue(sec))
        return SRect((w.x - to.x) / to.scale, (w.y - to.y) / to.scale, w.w / to.scale, w.h / to.scale)
    }

    /** Wraps one section; full stage size so touches reach it, moved and scaled by the turn. */
    @Composable
    fun Section(sec: Sec, content: @Composable () -> Unit) {
        val a = fromRect(sec)
        val b = rectsOf(to.landscape).getValue(sec)
        val density = androidx.compose.ui.platform.LocalDensity.current.density
        // S7: while the welcome plays, a section rises and fades in its turn (read in the layer block only)
        val welcome = LocalWelcome.current
        val rise = sec.rise()
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (rise != null && welcome.playing) {
                        alpha = welcome.alpha(rise)
                        translationY = welcome.lift(rise) * density
                    }
                    val p = progress.value
                    if (p < 1f) {
                        val s0 = (a.w / b.w).coerceIn(0.2f, 5f)
                        val s = s0 + (1f - s0) * p
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0f, 0f)
                        val px = a.x + (b.x - a.x) * p
                        val py = a.y + (b.y - a.y) * p
                        translationX = (px - s * b.x) * density
                        translationY += (py - s * b.y) * density
                        alpha *= (p * 2.2f).coerceIn(0f, 1f)
                    }
                },
        ) { content() }
    }
}

@Composable
internal fun rememberRotateGlide(landscape: Boolean, motion: Motion, from: StageGeo, to: StageGeo): RotateGlide {
    val first = remember { booleanArrayOf(true) }
    val progress = remember(landscape) {
        val start = if (first[0] || !motion.allowed) 1f else 0f
        first[0] = false
        Animatable(start)
    }
    LaunchedEffect(progress) {
        if (progress.value < 1f) progress.animateTo(1f, tween(GlideMs, easing = FastOutSlowInEasing))
    }
    return RotateGlide(progress, from, to)
}

/**
 * Asks Android for its quick cross-fade when the phone turns, instead of the slow turning picture, so the
 * glide shows. UNVERIFIED on One UI 5: if the phone ignores it, its own animation plays under ours.
 */
@Composable
internal fun RotationAnimationEffect() {
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window
        if (window == null) {
            Log.e("medieval", "Rotation: no activity window, system turn animation stays")
            return@SideEffect
        }
        val attrs = window.attributes
        if (attrs.rotationAnimation != WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE) {
            attrs.rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE
            window.attributes = attrs
        }
    }
}
