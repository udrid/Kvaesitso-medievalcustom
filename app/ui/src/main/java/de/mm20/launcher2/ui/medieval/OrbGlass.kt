package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import de.mm20.launcher2.ui.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * The Oracle's glass orb, owner's pick 2a-3 "heavy gold dust" (docs/design/T9-orb11.html, look "d").
 * Three pictures made by docs/design/T9-orbassets.html from the per-pixel glass renderer:
 * the smoke (with the gold dust) is turned slowly, the glass reflections are added on top and never
 * turn, the gold setting with rubies is drawn last. Drawing three small bitmaps at 10 fps is far cheaper
 * than computing the glass live on the GPU (see tasks/T9-vibe/handoff.md for the numbers).
 */

/** Smoke turn speed: degrees per second (T12: slowed from 2.2 after the owner found it too fast). */
private const val SmokeDegPerSec = 0.8f
/** Orb frame rate: 0.8 deg/s is 0.15 deg (about half a pixel at the rim) per frame, the twinkle period is ~4.8 s. */
private const val OrbFps = 5
private const val KindleMs = 1100L
private const val ChargePulseSec = 4.5f
private val Heart = Color(0xFFFFA060)
/** T15/T16: few grains (7), lit about 21 % of the time (only the top of each sine counts), gentle peak 0.4, period about 7 s. */
private const val TwinkleRate = 0.9f
private const val TwinkleFloor = 0.6f
private const val TwinkleGain = 0.4f
private val Twinkle = Color(0xFFFFF0C0)

/** A gold dust grain that twinkles: position in orb radii (turns with the smoke) and its own phase. */
private class Grain(val x: Float, val y: Float, val phase: Float, val size: Float)

@Composable
internal fun OrbGlass(modifier: Modifier = Modifier) {
    val motion = LocalMotion.current
    val smoke = ImageBitmap.imageResource(R.drawable.medieval_orb_smoke)
    val glass = ImageBitmap.imageResource(R.drawable.medieval_orb_glass)
    val setting = ImageBitmap.imageResource(R.drawable.medieval_orb_setting)
    val grains = remember {
        val r = Random(77)
        List(7) {
            val a = r.nextFloat() * 2f * PI.toFloat()
            val d = 0.17f + r.nextFloat() * 0.62f
            Grain(d * cos(a), d * sin(a), r.nextFloat() * 2f * PI.toFloat(), 0.6f + r.nextFloat() * 1.2f)
        }
    }
    // smoke turn, twinkle and charging pulse share one clock: one ambient effect
    val clock = rememberAmbientClock(motion.ambient, OrbFps)
    val kindle = rememberOneShot(motion.wakes, KindleMs, motion.ambient)
    val charging = motion.charging

    // its own layer, so only the orb is redrawn when the clock ticks
    Canvas(modifier.graphicsLayer()) {
        val t = clock.value / 1000f
        val w = size.width
        val r = w / 2f
        val dst = IntSize(w.toInt(), size.height.toInt())

        rotate(t * SmokeDegPerSec % 360f) {
            drawImage(smoke, IntOffset.Zero, IntSize(smoke.width, smoke.height), IntOffset.Zero, dst, filterQuality = FilterQuality.Medium)
            for (g in grains) {
                val v = max(0f, sin(t * TwinkleRate + g.phase) - TwinkleFloor) / (1f - TwinkleFloor)
                val a = TwinkleGain * v * v * v * v
                if (a > 0.02f) {
                    val c = Offset(center.x + g.x * r, center.y + g.y * r)
                    drawCircle(Twinkle.copy(alpha = 0.3f * a), radius = 0.022f * w * g.size, center = c)
                    drawCircle(Twinkle.copy(alpha = a), radius = 0.0055f * w * g.size, center = c)
                }
            }
        }

        // the heart: soft bloom and a small star; while charging it swells and fades slowly
        val pulse = if (charging) 0.5f - 0.5f * cos(2f * PI.toFloat() * t / ChargePulseSec) else 0f
        heart(w, 1f + 0.8f * pulse, 1f + 0.35f * pulse)

        // waking: the orb starts dark and the heart flares up (reflections stay)
        val k = kindle.value
        if (k > 0f) drawCircle(Color.Black.copy(alpha = 0.75f * (1f - k) * (1f - k)), radius = r)

        drawImage(glass, IntOffset.Zero, IntSize(glass.width, glass.height), IntOffset.Zero, dst, blendMode = BlendMode.Plus, filterQuality = FilterQuality.Medium)
        drawImage(setting, IntOffset.Zero, IntSize(setting.width, setting.height), IntOffset.Zero, dst, filterQuality = FilterQuality.Medium)
    }
}

/** Bloom + four-point star + bright point at the centre ([glow] scales brightness, [grow] the size). */
private fun DrawScope.heart(w: Float, glow: Float, grow: Float) {
    val bloom = 0.17f * w * grow
    drawCircle(
        Brush.radialGradient(
            0f to Heart.copy(alpha = (0.42f * glow).coerceAtMost(1f)),
            1f to Heart.copy(alpha = 0f),
            center = center, radius = bloom,
        ),
        radius = bloom,
    )
    val s = 0.075f * w * grow
    val q = 0.013f * w * grow
    val star = Path().apply {
        moveTo(center.x, center.y - s)
        quadraticTo(center.x + q, center.y - q, center.x + s, center.y)
        quadraticTo(center.x + q, center.y + q, center.x, center.y + s)
        quadraticTo(center.x - q, center.y + q, center.x - s, center.y)
        quadraticTo(center.x - q, center.y - q, center.x, center.y - s)
        close()
    }
    drawPath(star, Heart.copy(alpha = (0.5f * glow).coerceAtMost(1f)))
    drawCircle(Color(0xFFFFF6E0), radius = 0.022f * w)
    drawCircle(Color(0xFFFFFCF4), radius = 0.011f * w)
}
