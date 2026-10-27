package de.mm20.launcher2.ui.medieval

import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import de.mm20.launcher2.ui.R

/** Package name of the Claude Android app. UNVERIFIED until the owner tests the orb on the phone. */
internal const val ClaudePackage = "com.anthropic.claude"

private val RingPhrases = listOf("NOSCE TE IPSUM", "SAPERE AUDE", "LUX ET VERITAS")

/** Ring text (owner, 2026-10-26): each phrase gets a 120 degree third; [RingCrossGapDeg] of it holds the cross. */
private const val RingTextSize = 13f
private const val RingCrossGapDeg = 14f
private const val RingPhraseDeg = 120f - RingCrossGapDeg
private const val RingOuter = 86f
private const val RingInner = 57f

/** Halo round the orb: the oxblood-ember glow of the picked orb (was a flame orange). */
private val OrbHalo = Color(0x6BBE3C1E)

/**
 * Draws [text] letter by letter on the circle of [radius] around [cx], [cy], centered on [centerDeg]
 * (screen angle, 0 = right, clockwise) and spread evenly over [spanDeg]. Phrases in the lower half run
 * counter-clockwise with their tops toward the center so they stay upright. The middle of the
 * capitals ([capHeight]) sits on [radius].
 */
private fun drawSpreadArcText(
    canvas: android.graphics.Canvas, paint: Paint, text: String,
    cx: Float, cy: Float, radius: Float, centerDeg: Float, spanDeg: Float, capHeight: Float,
) {
    val lower = Math.sin(Math.toRadians(centerDeg.toDouble())) > 0.0
    val widths = text.map { paint.measureText(it.toString()) }
    val arcLen = Math.toRadians(spanDeg.toDouble()).toFloat() * radius
    // extra space between letters so the phrase fills its arc exactly (from first glyph edge to last)
    val gap = if (text.length > 1) (arcLen - widths.sum()) / (text.length - 1) else 0f
    var along = 0f
    text.forEachIndexed { i, ch ->
        val mid = along + widths[i] / 2f
        val offDeg = Math.toDegrees((mid / radius).toDouble()).toFloat()
        val deg = if (lower) centerDeg + spanDeg / 2f - offDeg else centerDeg - spanDeg / 2f + offDeg
        canvas.save()
        canvas.rotate(if (lower) deg - 90f else deg + 90f, cx, cy)
        // after the rotation the letter's spot is straight above (upper) or below (lower) the center
        val baseline = if (lower) cy + radius + capHeight / 2f else cy - radius + capHeight / 2f
        canvas.drawText(ch.toString(), cx, baseline, paint)
        canvas.restore()
        along += widths[i] + gap
    }
}

/** Small cross pattée centered on the band at [deg]; [s] is its half size, [dy] a vertical shift, drawn in [color]. */
private fun DrawScope.ringCross(cx: Float, cy: Float, radius: Float, deg: Float, s: Float, color: Color, dy: Float = 0f) {
    val a = Math.toRadians(deg.toDouble())
    val x = cx + radius * Math.cos(a).toFloat()
    val y = cy + radius * Math.sin(a).toFloat() + dy
    val w = s * 0.42f
    val p = Path().apply {
        // four flared arms meeting in the middle
        moveTo(x, y); lineTo(x - w, y - s); lineTo(x + w, y - s); close()
        moveTo(x, y); lineTo(x - w, y + s); lineTo(x + w, y + s); close()
        moveTo(x, y); lineTo(x - s, y - w); lineTo(x - s, y + w); close()
        moveTo(x, y); lineTo(x + s, y - w); lineTo(x + s, y + w); close()
    }
    drawPath(p, color)
}

/*
 * Engraved metal (owner's pick T9, docs/design/T9-ring.html style 2): every letter and cross is drawn
 * three times, a light edge just below, a dark lip just above and the darker metal of the cut on top,
 * then a thin dark line inside the cut. Offsets are in ring units, straight down the screen.
 */
private val EngraveLight = Color(0x80E3CF9C)
private val EngraveDark = Color(0xD9000000)
private val EngraveCut = Color(0xFF5E4A26)
private val EngraveLine = Color(0x73000000)
private const val EngraveLightDy = 0.65f
private const val EngraveDarkDy = -0.45f


/** The Oracle: ring with inscription, glowing orb, label. Tap opens the Claude app. */
@Composable
internal fun OracleOrb() {
    val context = LocalContext.current
    val ringFace = androidx.compose.runtime.remember {
        try {
            ResourcesCompat.getFont(context, R.font.cinzel_variable)
        } catch (e: Exception) {
            Log.e("medieval", "Could not load Cinzel for the orb ring", e)
            null
        }
    }

    // ring with inscription (176 x 176 on the body stage, drawn 8% larger with the body)
    Canvas(Modifier.at(92f, 118f).size(176.dp)) {
        val k = size.width / 176f
        drawCircle(Medieval.GoldDark, radius = RingOuter * k, center = center, style = Stroke(1.5f * k))
        drawCircle(Color(0xFF3A2E18), radius = RingInner * k, center = center, style = Stroke(1f * k))
        val bandMiddle = (RingOuter + RingInner) / 2f * k
        val c = size.width / 2f
        drawIntoCanvas { canvas ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = RingTextSize * k
                textAlign = Paint.Align.CENTER
                if (ringFace != null) typeface = ringFace
                // Cinzel is a variable font: ask for its real Bold instead of faking it
                if (!setFontVariationSettings("'wght' 700")) {
                    Log.e("medieval", "Cinzel weight 700 not applied to the orb ring")
                    isFakeBoldText = true
                }
            }
            // real cap height of this font, so the capitals sit in the middle of the band
            val bounds = android.graphics.Rect()
            paint.getTextBounds("H", 0, 1, bounds)
            val capHeight = bounds.height().toFloat()
            val native = canvas.nativeCanvas
            fun phrases(color: Color, dy: Float) {
                paint.color = color.toArgb()
                native.save()
                native.translate(0f, dy * k)
                RingPhrases.forEachIndexed { i, phrase ->
                    val phraseDeg = -90f + 120f * i
                    drawSpreadArcText(native, paint, phrase, c, c, bandMiddle, phraseDeg, RingPhraseDeg, capHeight)
                }
                native.restore()
            }
            phrases(EngraveLight, EngraveLightDy)
            phrases(EngraveDark, EngraveDarkDy)
            phrases(EngraveCut, 0f)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f * k
            phrases(EngraveLine, 0f)
        }
        // a small cross in the middle of each gap, engraved the same way
        for (i in 0 until 3) {
            val deg = -30f + 120f * i
            ringCross(c, c, bandMiddle, deg, 3.6f * k, EngraveLight, EngraveLightDy * k)
            ringCross(c, c, bandMiddle, deg, 3.6f * k, EngraveDark, EngraveDarkDy * k)
            ringCross(c, c, bandMiddle, deg, 3.6f * k, EngraveCut)
        }
    }

    // T9: a faint warm glint walks once round the ring every 20 s (Animations: Full only)
    RingLight(Modifier.at(92f, 118f).size(176.dp), (RingOuter + RingInner) / 2f, RingOuter - RingInner)

    // orb (124 at 133, 284): the glass orb of T9 (oxblood smoke with gold dust in a gold setting)
    val tap = rememberTapReaction()
    Box(
        Modifier
            .at(118f, 144f)
            .size(124.dp)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        0.6f to OrbHalo,
                        1f to Color.Transparent,
                        center = center,
                        radius = size.width * 0.75f,
                    ),
                    radius = size.width * 0.75f,
                )
            }
            // after the halo, so the flash's layer does not cut the halo square
            .tapReaction(tap)
            .clip(CircleShape)
            .border(2.dp, Medieval.GoldDeep, CircleShape)
            .clickable(interactionSource = tap.source, indication = null) { tap.open { openClaude(context) } },
    ) {
        OrbGlass(Modifier.matchParentSize())
    }
}

internal fun openClaude(context: android.content.Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(ClaudePackage)
    if (intent == null) {
        Log.e("medieval", "Claude app ($ClaudePackage) has no launch intent")
        Toast.makeText(context, "The Claude app is not installed", Toast.LENGTH_SHORT).show()
        return
    }
    launchSafely(context, intent, "the Claude app")
}

/** Duration of one sweep of the ring light, and the rest before the next (20 s in all). */
private const val RingSweepMs = 7_000L
private const val RingRestMs = 13_000L

/**
 * The ring light (T9, Animations: Full): a warm glint walks once round the inscription band
 * ([bandMiddle], [bandWidth] in ring units of 176), then rests. It only redraws while it is visible.
 */
@Composable
private fun RingLight(modifier: Modifier, bandMiddle: Float, bandWidth: Float) {
    val motion = LocalMotion.current
    val sweep = remember { mutableFloatStateOf(-1f) }
    LaunchedEffect(motion.ringLight) {
        sweep.floatValue = -1f
        if (!motion.ringLight) return@LaunchedEffect
        try {
            while (true) {
                delay(RingRestMs)
                val start = withFrameMillis { it }
                while (true) {
                    val e = withFrameMillis { it } - start
                    if (e >= RingSweepMs) break
                    sweep.floatValue = e.toFloat() / RingSweepMs
                    delay(1000L / AmbientFps)
                }
                sweep.floatValue = -1f
            }
        } finally {
            sweep.floatValue = -1f
        }
    }
    Canvas(modifier.graphicsLayer()) {
        val p = sweep.floatValue
        if (p < 0f) return@Canvas
        val k = size.width / 176f
        val fade = sin(PI.toFloat() * p)
        val glint = Color(0xFFFFE2A0)
        rotate(-30f + 360f * p) {
            drawCircle(
                Brush.sweepGradient(
                    0f to glint.copy(alpha = 0f),
                    0.06f to glint.copy(alpha = 0.34f * fade),
                    0.14f to glint.copy(alpha = 0f),
                    1f to glint.copy(alpha = 0f),
                    center = center,
                ),
                radius = bandMiddle * k,
                style = Stroke(bandWidth * k),
                blendMode = BlendMode.Plus,
            )
        }
    }
}
