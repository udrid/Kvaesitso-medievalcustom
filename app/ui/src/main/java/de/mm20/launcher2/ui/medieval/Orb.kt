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

/** Small cross pattée centered on the band at [deg]; [s] is its half size. */
private fun DrawScope.ringCross(cx: Float, cy: Float, radius: Float, deg: Float, s: Float) {
    val a = Math.toRadians(deg.toDouble())
    val x = cx + radius * Math.cos(a).toFloat()
    val y = cy + radius * Math.sin(a).toFloat()
    val w = s * 0.42f
    val p = Path().apply {
        // four flared arms meeting in the middle
        moveTo(x, y); lineTo(x - w, y - s); lineTo(x + w, y - s); close()
        moveTo(x, y); lineTo(x - w, y + s); lineTo(x + w, y + s); close()
        moveTo(x, y); lineTo(x - s, y - w); lineTo(x - s, y + w); close()
        moveTo(x, y); lineTo(x + s, y - w); lineTo(x + s, y + w); close()
    }
    drawPath(p, Medieval.GoldDeep)
}


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
                color = Medieval.GoldDeep.toArgb()
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
            RingPhrases.forEachIndexed { i, phrase ->
                val phraseDeg = -90f + 120f * i
                drawSpreadArcText(canvas.nativeCanvas, paint, phrase, c, c, bandMiddle, phraseDeg, RingPhraseDeg, capHeight)
            }
        }
        // a small cross in the middle of each gap
        for (i in 0 until 3) ringCross(c, c, bandMiddle, -30f + 120f * i, 3.6f * k)
    }

    // orb (124 at 133, 284)
    Box(
        Modifier
            .at(118f, 144f)
            .size(124.dp)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        0.6f to Color(0x42E58A3C),
                        1f to Color.Transparent,
                        center = center,
                        radius = size.width * 0.75f,
                    ),
                    radius = size.width * 0.75f,
                )
            }
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    0f to Color(0xFF4A2D14),
                    0.72f to Color(0xFF0F0904),
                    center = Offset(62f * 3f, 47f * 3f),
                    radius = 124f * 3f,
                )
            )
            .border(2.dp, Medieval.GoldDeep, CircleShape)
            .clickable { openClaude(context) },
        contentAlignment = Alignment.Center,
    ) {
        SvgIcon(MedievalIcons.Flame, 54f, Medieval.Ember, strokeWidth = 1.4f)
    }

    MText(
        "The Oracle", 19f, Medieval.Text,
        Modifier.at(80f, 296f).width(200.dp),
        font = Medieval.Blackletter, bold = true, align = TextAlign.Center,
    )
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
