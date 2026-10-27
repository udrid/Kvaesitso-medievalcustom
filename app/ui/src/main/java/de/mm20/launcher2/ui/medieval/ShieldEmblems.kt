package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** How one path of an emblem is painted (docs/design/T7-shields.html). */
internal enum class EmblemPaint { Line, GoldFill, WaxFill, Dot }

internal class EmblemPart(val path: String, val paint: EmblemPaint = EmblemPaint.Line)

private fun line(d: String) = EmblemPart(d)
private fun gold(d: String) = EmblemPart(d, EmblemPaint.GoldFill)
private fun wax(d: String) = EmblemPart(d, EmblemPaint.WaxFill)
private fun dot(d: String) = EmblemPart(d, EmblemPaint.Dot)

/** Heraldic line emblems on a 24 x 24 view box, one per well-known app. */
internal object ShieldEmblems {
    val Raven = listOf(
        line("M3 14c3-1 5-3 6-6 2 1 4 1 6 0l5 3-4 1c0 4-3 7-8 7H6l2-3-5-2z"),
        line("M10 13c2 0 4 1 5 3"),
        dot(svgCircle(14.2f, 9.6f, 0.7f)),
    )
    val Goblet = listOf(
        line("M6.5 4h11c0 5.5-1.5 8.5-5.5 9.5C8 12.5 6.5 9.5 6.5 4z"),
        line("M6.8 7.5h10.4"),
        line("M12 13.5V19"),
        line("M8 20.5h8"),
    )
    val Scroll = listOf(
        line("M6 6.5h12v11H6z"),
        line("M3.6 6.2a1.4 1.4 0 0 1 2.8 0v11.6a1.4 1.4 0 0 1-2.8 0z"),
        line("M17.6 6.2a1.4 1.4 0 0 1 2.8 0v11.6a1.4 1.4 0 0 1-2.8 0z"),
        gold("M10.6 9.6l4 2.4-4 2.4z"),
    )
    val SealedLetter = listOf(
        line("M3.5 6h17v12h-17z"),
        line("M3.5 6.5l8.5 6.2 8.5-6.2"),
        wax(svgCircle(12f, 13f, 2.6f)),
    )
    val CompassRose = listOf(
        line(svgCircle(12f, 12f, 9f)),
        line("M12 5.2l1.8 5 5 1.8-5 1.8-1.8 5-1.8-5-5-1.8 5-1.8z"),
        line("M12 1.6v2.2"),
    )
    val Bell = listOf(
        line("M12 4c-3 0-5 2.5-5 6v4l-2 3h14l-2-3v-4c0-3.5-2-6-5-6z"),
        line("M10 19a2 2 0 0 0 4 0"),
        line("M12 2.4V4"),
    )
    val Eye = listOf(
        line("M2.5 12C5 7.6 8.5 6 12 6s7 1.6 9.5 6c-2.5 4.4-6 6-9.5 6s-7-1.6-9.5-6z"),
        line(svgCircle(12f, 12f, 3f)),
        dot(svgCircle(12f, 12f, 0.9f)),
    )
    val Orb = listOf(
        line(svgCircle(12f, 14.5f, 7f)),
        line("M5 14.5h14"),
        line("M12 7.5c-4.2 3.5-4.2 10.5 0 14 4.2-3.5 4.2-10.5 0-14z"),
        line("M12 1.8v4M10.2 3.6h3.6"),
    )
    val Portrait = listOf(
        line("M3.5 4.5h17v15h-17z"),
        line("M6 7h12v10H6z"),
        line("M6 17l4.2-5 3 3.4 2-2L18 17"),
        line(svgCircle(15.4f, 9.8f, 1.1f)),
    )
    val Wheel = listOf(
        line(svgCircle(12f, 12f, 6.8f)),
        line(svgCircle(12f, 12f, 2f)),
        line("M12 1.8v8.2M12 14v8.2M1.8 12h8.2M14 12h8.2"),
        line("M13.4 10.6l5.8-5.8M10.6 10.6L4.8 4.8M13.4 13.4l5.8 5.8M10.6 13.4l-5.8 5.8"),
    )
    val Hourglass = listOf(
        line("M6.5 3h11M6.5 21h11"),
        line("M8 3v3.5l4 5.5-4 5.5V21M16 3v3.5l-4 5.5 4 5.5V21"),
        gold("M9.8 19.2h4.4L12 16z"),
    )
    val BookOfDays = listOf(
        line("M5 4.5a2 2 0 0 1 2-2h11.5v16.8H7a2 2 0 0 0-2 2z"),
        line("M5 19.3a2 2 0 0 1 2-2h11.5"),
        wax("M14.5 2.5v6l1.6-1.3 1.6 1.3v-6"),
    )
    val Lute = listOf(
        line(svgCircle(8.6f, 15.4f, 5.3f)),
        line(svgCircle(8.6f, 15.4f, 1.5f)),
        line("M12.3 11.7L19 5"),
        line("M17.6 3.6l2.8 2.8"),
    )
    val Trumpet = listOf(
        line("M3 10.8h11.5M3 13.2h11.5"),
        line("M14.5 10.8L21 7.2v9.6l-6.5-3.6"),
        line("M3 10.2v3.6"),
        wax("M7.5 13.2v6l2-1.3 2 1.3v-6"),
    )
    val QuillPen = listOf(
        line("M20 4C12 4.2 7 9 6.4 16.5L4.2 20l3.4-2.2C15 17.2 19.8 12.2 20 4z"),
        line("M7 17.2L15.2 8.8"),
    )

    private val byPackage: Map<String, List<EmblemPart>> = buildMap {
        fun add(parts: List<EmblemPart>, vararg packages: String) = packages.forEach { put(it, parts) }
        add(Raven, "com.twitter.android")
        add(Goblet, "com.discord")
        add(Scroll, "com.google.android.youtube")
        add(SealedLetter, "com.samsung.android.messaging", "com.google.android.apps.messaging", "com.android.mms")
        add(CompassRose, "com.google.android.apps.maps")
        add(Bell, "com.samsung.android.dialer", "com.google.android.dialer", "com.android.dialer")
        add(Eye, "com.sec.android.app.camera", "com.android.camera", "com.android.camera2", "com.google.android.GoogleCamera")
        add(
            Orb, "com.android.chrome", "com.brave.browser", "com.sec.android.app.sbrowser",
            "org.mozilla.firefox", "com.microsoft.emmx", "com.opera.browser",
        )
        add(Portrait, "com.sec.android.gallery3d", "com.samsung.android.gallery", "com.google.android.apps.photos")
        add(Wheel, "com.android.settings")
        add(Hourglass, "com.sec.android.app.clockpackage", "com.google.android.deskclock")
        add(BookOfDays, "com.samsung.android.calendar", "com.google.android.calendar")
        add(Lute, "com.spotify.music")
        add(Trumpet, "com.whatsapp", "com.whatsapp.w4b")
        add(QuillPen, "com.google.android.gm")
    }

    /** The emblem of a package, or null when the app has none (use [LetterShield]). */
    fun forPackage(packageName: String): List<EmblemPart>? = byPackage[packageName]
}

/** Gold line-art emblem on a 24 x 24 view box. */
@Composable
internal fun EmblemIcon(parts: List<EmblemPart>, size: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size.dp)) {
        val k = this.size.width / 24f
        val stroke = Stroke(width = 1.6f * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        for (part in parts) {
            val p = PathParser().parsePathString(part.path).toPath()
            val m = androidx.compose.ui.graphics.Matrix()
            m.scale(k, k)
            p.transform(m)
            when (part.paint) {
                EmblemPaint.Line -> drawPath(p, Medieval.Gold, style = stroke)
                EmblemPaint.GoldFill -> {
                    drawPath(p, Medieval.Gold)
                    drawPath(p, Medieval.Gold, style = stroke)
                }
                EmblemPaint.WaxFill -> {
                    drawPath(p, Color(0xFF8A2A20))
                    drawPath(p, Medieval.Gold, style = stroke)
                }
                EmblemPaint.Dot -> drawPath(p, Medieval.Gold)
            }
        }
    }
}

/** Fallback for apps with no emblem: small gold shield with the first letter of the app name in blackletter. */
@Composable
internal fun LetterShield(label: String, modifier: Modifier = Modifier) {
    val letter = label.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "?"
    Box(modifier.size(26.dp, 30.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(26.dp, 30.dp)) {
            val k = size.width / 26f
            val shield = Path().apply {
                moveTo(13f * k, 1f * k); lineTo(24f * k, 5f * k); lineTo(24f * k, 14f * k)
                cubicTo(24f * k, 21f * k, 19f * k, 26f * k, 13f * k, 28.5f * k)
                cubicTo(7f * k, 26f * k, 2f * k, 21f * k, 2f * k, 14f * k)
                lineTo(2f * k, 5f * k); close()
            }
            val inner = Path().apply {
                moveTo(13f * k, 3.6f * k); lineTo(21.8f * k, 6.7f * k); lineTo(21.8f * k, 14f * k)
                cubicTo(21.8f * k, 19.8f * k, 17.8f * k, 23.8f * k, 13f * k, 26f * k)
                cubicTo(8.2f * k, 23.8f * k, 4.2f * k, 19.8f * k, 4.2f * k, 14f * k)
                lineTo(4.2f * k, 6.7f * k); close()
            }
            drawPath(
                shield,
                Brush.verticalGradient(
                    0f to Color(0xFFE3CF9C), 0.55f to Medieval.Gold, 1f to Medieval.GoldDeep,
                    startY = 0f, endY = size.height,
                ),
            )
            drawPath(shield, Color(0xFFE3CF9C), style = Stroke(k, join = StrokeJoin.Round))
            drawPath(inner, Medieval.GoldDark, style = Stroke(0.7f * k))
        }
        MText(
            letter, 17f, Medieval.ShieldFill, Modifier.offset(0.dp, 1.dp),
            font = Medieval.Blackletter, bold = true,
        )
    }
}

/** What a shield shows for [packageName]: its emblem, or the letter shield. Never the app's own icon. */
@Composable
internal fun ShieldEmblem(packageName: String, label: String) {
    val parts = ShieldEmblems.forPackage(packageName)
    if (parts != null) EmblemIcon(parts, 26f) else LetterShield(label)
}
