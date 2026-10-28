package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

/** Stage coordinates (C5 is laid out in 390 x 844 px) -> dp offset. */
internal fun Modifier.at(x: Float, y: Float): Modifier = this.offset(x.dp, y.dp)

/** Text with sizes in stage px, so the stage scales with the screen and ignores font scale. */
@Composable
internal fun MText(
    text: String,
    size: Float,
    color: Color,
    modifier: Modifier = Modifier,
    font: FontFamily = Medieval.FellEnglish,
    bold: Boolean = false,
    italic: Boolean = false,
    spacingEm: Float = 0f,
    align: TextAlign? = null,
    maxLines: Int = 1,
    lineHeight: Float = 0f,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size.sp,
        fontFamily = font,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        letterSpacing = (spacingEm * size).sp,
        textAlign = align,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = if (lineHeight > 0f) TextStyle(lineHeight = lineHeight.sp) else TextStyle(),
    )
}

/** Small uppercase Cinzel label ("cap" in C5.html). */
@Composable
internal fun Cap(
    text: String,
    modifier: Modifier = Modifier,
    size: Float = 9.5f,
    color: Color = Medieval.Cap,
    spacingEm: Float = 0.24f,
) = MText(text.uppercase(), size, color, modifier, Medieval.Cinzel, bold = true, spacingEm = spacingEm)

/** Circle as SVG path data. */
internal fun svgCircle(cx: Float, cy: Float, r: Float) =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

/** Line icon drawn from SVG path data on a 24 x 24 view box (or [viewBox]). */
@Composable
internal fun SvgIcon(
    paths: List<String>,
    size: Float,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 1.6f,
    viewBox: Float = 24f,
) {
    Canvas(modifier.size(size.dp)) {
        val k = this.size.width / viewBox
        val stroke = Stroke(
            width = strokeWidth * k,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        for (d in paths) {
            val p = PathParser().parsePathString(d).toPath()
            val m = androidx.compose.ui.graphics.Matrix()
            m.scale(k, k)
            p.transform(m)
            drawPath(p, color, style = stroke)
        }
    }
}

private class RibbonShape : Shape {
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

/** Parchment panel with red ribbon tab. Positioned at [top] on the body stage (361 wide), 16 px from both sides. */
@Composable
internal fun Panel(
    top: Float,
    height: Float,
    modifier: Modifier = Modifier,
    ribbon: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .at(16f, top)
            .size(329.dp, height.dp)
            .panelLook()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        content()
        if (ribbon != null) {
            Box(
                Modifier
                    .offset(12.dp, (-8).dp)
                    .height(17.dp)
                    .clip(RibbonShape())
                    .background(Brush.verticalGradient(listOf(Medieval.Oxblood, Medieval.OxbloodDark)))
                    .drawBehind {
                        drawLine(Medieval.Gold, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                    }
                    .padding(start = 11.dp, end = 17.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                MText(
                    ribbon.uppercase(), 8.5f, Medieval.RibbonText,
                    font = Medieval.Cinzel, bold = true, spacingEm = 0.24f,
                )
            }
        }
    }
}

private fun Modifier.panelLook(): Modifier = this
    .background(Brush.verticalGradient(listOf(Medieval.PanelTop, Medieval.PanelBottom)))
    .drawBehind {
        val edge = 1.dp.toPx()
        drawRect(
            Medieval.GoldDark,
            topLeft = Offset(edge / 2f, edge / 2f),
            size = Size(size.width - edge, size.height - edge),
            style = Stroke(edge),
        )
        val inset = 4.dp.toPx()
        drawRect(
            Medieval.GoldDeep.copy(alpha = 0.26f),
            topLeft = Offset(inset, inset),
            size = Size(size.width - 2 * inset, size.height - 2 * inset),
            style = Stroke(1.dp.toPx()),
        )
    }

/** Starts an activity and reports a failure instead of crashing. */
internal fun launchSafely(context: Context, intent: Intent, what: String) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        (intent.component?.packageName ?: intent.`package`)?.let { UsageCounter.bump(context, it) }
    } catch (e: Exception) {
        Log.e("medieval", "Could not open $what", e)
        Toast.makeText(context, "Could not open $what", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Like produceState, but [block] runs only while Home is resumed (T17): it is cancelled when Home pauses
 * or the screen goes off and starts again, reading first thing, when Home comes back.
 */
@Composable
internal fun <T> produceWhileResumed(initial: T, block: suspend androidx.compose.runtime.ProduceStateScope<T>.() -> Unit): State<T> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    return produceState(initial, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { block() }
    }
}

/** Current time, refreshed at the start of every minute while Home is resumed (and at once when it resumes). */
@Composable
internal fun rememberNow(): State<ZonedDateTime> =
    produceWhileResumed(ZonedDateTime.now()) {
        while (true) {
            val now = ZonedDateTime.now()
            value = now
            delay(60_000L - System.currentTimeMillis() % 60_000L + 50L)
        }
    }
