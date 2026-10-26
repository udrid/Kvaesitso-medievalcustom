package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import kotlinx.coroutines.delay

/**
 * Tiny vector toolkit for the T4 drawings (docs/design/T4-header.html, T4-weather.js).
 * A drawing is a list of [Part]s in paint order; a part with several frames is a stop-motion
 * animation that swaps frames in hard steps.
 */
internal class Prim(
    val path: Path,
    val fill: Color? = null,
    val stroke: Color? = null,
    val width: Float = 1f,
    val alpha: Float = 1f,
    val brush: Brush? = null,
    val dash: FloatArray? = null,
)

internal class Part(val frames: List<List<Prim>>, val periodMs: Long = 0L)

internal fun still(prims: List<Prim>) = Part(listOf(prims))
internal fun anim(periodMs: Long, frames: List<List<Prim>>) = Part(frames, periodMs)

/** SVG path data -> [Prim]. Transforms run like SVG "translate rotate scale": scale first, then rotate, then move. */
internal fun prim(
    d: String,
    fill: Color? = null,
    stroke: Color? = null,
    width: Float = 1f,
    alpha: Float = 1f,
    scale: Float = 1f,
    scaleX: Float = scale,
    rotate: Float = 0f,
    dx: Float = 0f,
    dy: Float = 0f,
    brush: Brush? = null,
    dash: FloatArray? = null,
): Prim {
    val p = PathParser().parsePathString(d).toPath()
    if (scaleX != 1f || scale != 1f) p.transform(Matrix().apply { scale(scaleX, scale) })
    if (rotate != 0f) p.transform(Matrix().apply { rotateZ(rotate) })
    if (dx != 0f || dy != 0f) p.translate(Offset(dx, dy))
    return Prim(p, fill, stroke, width, alpha, brush, dash)
}

internal fun circlePath(cx: Float, cy: Float, r: Float) = svgCircle(cx, cy, r)

internal fun ellipsePath(cx: Float, cy: Float, rx: Float, ry: Float) =
    "M${cx - rx} ${cy}a$rx $ry 0 1 0 ${2 * rx} 0a$rx $ry 0 1 0 ${-2 * rx} 0z"

internal fun rectPath(x: Float, y: Float, w: Float, h: Float) = "M$x ${y}h${w}v${h}h${-w}z"

internal fun DrawScope.drawParts(parts: List<Part>, timeMs: Long) {
    for (part in parts) {
        val n = part.frames.size
        val list = if (n == 1 || part.periodMs <= 0L) part.frames[0]
        else part.frames[((timeMs % part.periodMs) * n / part.periodMs).toInt().coerceIn(0, n - 1)]
        for (p in list) {
            if (p.brush != null) drawPath(p.path, p.brush, alpha = p.alpha)
            else if (p.fill != null) drawPath(p.path, p.fill, alpha = p.alpha)
            if (p.stroke != null) {
                drawPath(
                    p.path, p.stroke, alpha = p.alpha,
                    style = Stroke(
                        width = p.width,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = p.dash?.let { PathEffect.dashPathEffect(it) },
                    ),
                )
            }
        }
    }
}

/**
 * Draws [parts] laid out on a view box of [viewW] units wide whose top-left corner is ([originX], [originY]).
 * With [animated], the stop-motion frames advance; the clock only ticks while the screen is drawing frames.
 */
@Composable
internal fun VectorCanvas(
    parts: List<Part>,
    viewW: Float,
    modifier: Modifier = Modifier,
    originX: Float = 0f,
    originY: Float = 0f,
    animated: Boolean = false,
) {
    val time = remember { mutableLongStateOf(0L) }
    if (animated) {
        LaunchedEffect(Unit) {
            val start = withFrameMillis { it }
            while (true) {
                withFrameMillis { time.longValue = it - start }
                delay(80L)
            }
        }
    }
    Canvas(modifier) {
        val k = size.width / viewW
        withTransform({
            scale(k, k, pivot = Offset.Zero)
            translate(-originX, -originY)
        }) {
            drawParts(parts, time.longValue)
        }
    }
}
