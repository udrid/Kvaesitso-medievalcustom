package de.mm20.launcher2.ui.medieval

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The seven planetary days of the T4 header (docs/design/T4-header.html): for each weekday its planet,
 * a medallion (alchemical sign inside a ring) and the ornamented divider under the header.
 */
internal enum class Planet(val label: String) { Sol("Sol"), Luna("Luna"), Mars("Mars"), Mercury("Mercury"), Jupiter("Jupiter"), Venus("Venus"), Saturn("Saturn") }

internal fun planetOf(day: DayOfWeek): Planet = when (day) {
    DayOfWeek.SUNDAY -> Planet.Sol
    DayOfWeek.MONDAY -> Planet.Luna
    DayOfWeek.TUESDAY -> Planet.Mars
    DayOfWeek.WEDNESDAY -> Planet.Mercury
    DayOfWeek.THURSDAY -> Planet.Jupiter
    DayOfWeek.FRIDAY -> Planet.Venus
    DayOfWeek.SATURDAY -> Planet.Saturn
}

private val G = Medieval.Gold
private val GD = Medieval.GoldDeep
private val GL = Color(0xFFE3CF9C)
private val RD = Medieval.Rubric
private val Feather = Color(0xFF2A2216)
private val PetalRed = Color(0xFF5A1A14)

private fun f(v: Float) = "%.2f".format(java.util.Locale.ROOT, v)

private fun rays(n: Int, r1: Float, r2: Float, r3: Float, fill: Color, dx: Float = 0f, dy: Float = 0f): List<Prim> =
    (0 until n).map { i ->
        val a = i * 2 * PI / n
        val b = a + PI / n * 0.55
        val c = a - PI / n * 0.55
        val l = if (i % 2 == 1) r2 else r3
        prim(
            "M${f((r1 * cos(b)).toFloat())} ${f((r1 * sin(b)).toFloat())}" +
                "L${f((l * cos(a)).toFloat())} ${f((l * sin(a)).toFloat())}" +
                "L${f((r1 * cos(c)).toFloat())} ${f((r1 * sin(c)).toFloat())}Z",
            fill = fill, dx = dx, dy = dy,
        )
    }

private fun star4(x: Float, y: Float, r: Float) = prim(
    "M$x ${y - r}Q$x $y ${x + r} ${y}Q$x $y $x ${y + r}Q$x $y ${x - r} ${y}Q$x $y $x ${y - r}Z", fill = G,
)

private fun feathers(sd: Float): List<Prim> = (0 until 4).map { i ->
    val y = -6f + i * 5
    val x0 = 26f
    val x1 = 33f - i * 1.8f
    val y1 = y - 9 + i * 3.2f
    prim(
        "M${sd * x0} ${y}Q${sd * (x0 + 7)} ${y - 8} ${sd * x1} ${f(y1)}Q${sd * (x1 - 2)} ${f(y1 + 8)} ${sd * x0} ${y + 3}Z",
        fill = Feather, stroke = G, width = 1f,
    )
}

private fun ring(r: Float, color: Color, width: Float, dash: FloatArray? = null) =
    prim(circlePath(0f, 0f, r), stroke = color, width = width, dash = dash)

private fun ringOrnament(p: Planet): List<Prim> = when (p) {
    Planet.Sol -> rays(32, 26f, 31f, 34f, GD) + ring(25f, G, 1.2f, floatArrayOf(2f, 2.4f))
    Planet.Luna -> listOf(ring(27f, GD, 1f, floatArrayOf(1f, 3f))) +
        listOf(-31f to -20f, 29.5f to -18f, -30f to 18f, 30.5f to 17f, 0f to -33f, 0f to 33f)
            .zip(listOf(3f, 3.6f, 2.4f, 2.8f, 2.6f, 2.6f)) { (x, y), r -> star4(x, y, r) } +
        prim("M-30 -5A5 5 0 1 0 -30 5A3.8 3.8 0 1 1 -30 -5Z", fill = G, dx = 1.5f) +
        prim("M30 -5A5 5 0 1 1 30 5A3.8 3.8 0 1 0 30 -5Z", fill = G, dx = -1.5f)
    Planet.Mars -> (0 until 8).flatMap { i ->
        val rot = i * 45f
        listOf(prim("M26 0L33 0", stroke = GD, width = 1f, rotate = rot)) +
            if (i % 2 == 1) prim("M27 -3.5L33 0L27 3.5Z", fill = GD, rotate = rot)
            else prim("M27 -4.5L33.2 0L27 4.5L30 0Z", fill = RD, stroke = G, width = 0.9f, rotate = rot)
    } + ring(25f, G, 1.2f)
    Planet.Mercury -> feathers(1f) + feathers(-1f) + ring(26f, GD, 1f)
    Planet.Jupiter -> listOf(45f, 135f, 225f, 315f).map {
        prim("M24 -2L30 -5L29 -1L36 -3L28 4L29 0L24 2Z", fill = G, rotate = it)
    } + listOf(0f, 90f, 180f, 270f).map {
        prim(circlePath(30f, 0f, 1.8f), fill = RD, stroke = G, width = 0.7f, rotate = it)
    } + ring(25f, G, 1.2f) + ring(28f, GD, 0.8f, floatArrayOf(1f, 2.6f))
    Planet.Venus -> (0 until 10).map { i ->
        prim(
            "M25 0C27 -5 30.5 -5 32 0C30.5 5 27 5 25 0Z",
            fill = if (i % 2 == 1) Feather else PetalRed, stroke = G, width = 0.9f, rotate = i * 36f,
        )
    } + ring(25f, G, 1.2f)
    Planet.Saturn -> listOf(
        prim("M0 -31L9 -25L25 -20L31 0L25 20L9 25L0 31L-9 25L-25 20L-31 0L-25 -20L-9 -25Z", stroke = GD, width = 1f),
    ) + (0 until 12).map { prim("M26 0H31", stroke = G, width = 1.4f, rotate = it * 30f) } +
        listOf(0f, 90f, 180f, 270f).map {
            prim("M29 -2.8L33 0L29 2.8L25 0Z", fill = RD, stroke = G, width = 0.8f, rotate = it)
        } + ring(25f, G, 1.2f)
}

/** Alchemical sign, centred on 0,0, about 26 units tall before the 0.78 scale. */
private fun sign(p: Planet): List<Prim> {
    val s = 0.78f
    val w = 1.7f * s
    fun line(d: String, fill: Color? = null, alpha: Float = 1f) = prim(d, fill = fill, stroke = GL, width = w, scale = s, alpha = alpha)
    return when (p) {
        Planet.Sol -> listOf(line(circlePath(0f, 0f, 9.5f)), prim(circlePath(0f, 0f, 2f), fill = G, stroke = GL, width = w, scale = s))
        Planet.Luna -> listOf(line("M5 -12A12 12 0 1 0 5 12A9 9 0 1 1 5 -12Z", fill = G.copy(alpha = 0.85f)))
        Planet.Mars -> listOf(line(circlePath(-2.5f, 3.5f, 7.5f)), line("M3 -2L11 -10M4 -10H11V-3"))
        Planet.Mercury -> listOf(line(circlePath(0f, 2f, 5.2f)), line("M0 7.2V14M-3.4 10.8H3.4M-6.5 -11A6.5 6.5 0 0 0 6.5 -11"))
        Planet.Jupiter -> listOf(line("M-9 -6C-9 -13 -1 -13 -1 -6C-1 -1 -6 3 -11 4H10M4.5 -12V13"))
        Planet.Venus -> listOf(line(circlePath(0f, -4.5f, 6.5f)), line("M0 2V13M-4.5 7.5H4.5"))
        Planet.Saturn -> listOf(line("M-6 -11H0M-3 -14V3M-3 -4C0 -7 6 -5 6 0C6 5 2 7 2 12"))
    }
}

/** Medallion on a view box from (-44,-35), 88 x 70 units. */
internal fun medallion(p: Planet): List<Part> {
    val disc = prim(
        circlePath(0f, 0f, 21f),
        brush = Brush.radialGradient(
            listOf(Color(0xFF3A2A14), Color(0xFF0F0904)), center = Offset(0f, -4.2f), radius = 29.4f,
        ),
    )
    val discEdge = prim(circlePath(0f, 0f, 21f), stroke = GD, width = 1.4f)
    // the soft ember glow of the sign (CSS drop-shadow in the design) as a wide faint stroke
    val glow = sign(p).map { Prim(it.path, stroke = Medieval.Ember, width = 3.2f, alpha = 0.16f) }
    return listOf(still(ringOrnament(p) + disc + discEdge + glow + sign(p)))
}

/** Divider under the header, 346 x 14 units. */
internal fun dayRule(p: Planet): List<Part> {
    val w = 346f
    val c = 173f
    val out = mutableListOf(prim("M0 7H${c - 34}M${c + 34} 7H$w", stroke = Medieval.GoldLine, width = 1f))
    when (p) {
        Planet.Sol -> {
            for (i in 1..6) {
                val x = c - 34 - i * 24
                out += prim(circlePath(x, 7f, 1.6f), fill = GD)
                out += prim(circlePath(w - x, 7f, 1.6f), fill = GD)
            }
            out += rays(12, 4f, 5.5f, 8f, G, dx = c, dy = 7f)
            out += prim(circlePath(c, 7f, 3.2f), fill = RD, stroke = G, width = 1f)
        }
        Planet.Luna -> {
            val moon = "M2 -5A5 5 0 1 0 2 5A3.8 3.8 0 1 1 2 -5Z"
            for (i in 1..5) {
                val x = c - 34 - i * 28
                out += prim(moon, fill = G, dx = x, dy = 7f)
                out += prim(moon, fill = G, scale = 1f, scaleX = -1f, dx = w - x, dy = 7f)
            }
            out += star4(c, 7f, 6f)
            out += prim(circlePath(c - 16, 7f, 1.3f), fill = G)
            out += prim(circlePath(c + 16, 7f, 1.3f), fill = G)
        }
        Planet.Mars -> {
            for (i in 1..9) {
                val x = c - 34 - i * 15
                out += prim("M${x + 4} 2L$x 7L${x + 4} 12", stroke = GD, width = 1.3f)
                out += prim("M${w - x - 4} 2L${w - x} 7L${w - x - 4} 12", stroke = GD, width = 1.3f)
            }
            out += prim("M${c - 30} 7H${c + 30}", stroke = G, width = 1.5f)
            out += prim("M${c - 30} 3V11M${c - 34} 5L${c - 30} 3L${c - 26} 5M${c + 30} 3V11", stroke = G, width = 1.5f)
            out += prim("M${c - 7} 7L$c 1L${c + 7} 7L$c 13Z", fill = RD, stroke = G, width = 1f)
        }
        Planet.Mercury -> {
            for (i in 1..10) {
                val x = c - 34 - i * 14
                out += prim("M$x 11Q${x + 2} 5 ${x + 9} 3", stroke = GD, width = 1.2f)
                out += prim("M${w - x} 11Q${w - x - 2} 5 ${w - x - 9} 3", stroke = GD, width = 1.2f)
            }
            out += prim("M${c - 30} 7Q${c - 14} -2 $c 7Q${c + 14} 16 ${c + 30} 7", stroke = G, width = 1.3f)
            out += prim("M${c - 30} 7Q${c - 14} 16 $c 7Q${c + 14} -2 ${c + 30} 7", stroke = G, width = 1.3f)
            out += prim(circlePath(c, 7f, 3f), fill = RD, stroke = G, width = 1f)
        }
        Planet.Jupiter -> {
            for (i in 1..4) {
                val x = c - 34 - i * 38
                out += prim("M${x + 10} 1L${x + 3} 7L${x + 8} 7L$x 13", stroke = GD, width = 1.4f)
                out += prim("M${w - x - 10} 1L${w - x - 3} 7L${w - x - 8} 7L${w - x} 13", stroke = GD, width = 1.4f)
            }
            out += prim("M${c + 5} -1L${c - 4} 7H${c + 2}L${c - 6} 15", stroke = G, width = 2f)
            out += prim(circlePath(c - 22, 7f, 2.4f), fill = RD, stroke = G, width = 1f)
            out += prim(circlePath(c + 22, 7f, 2.4f), fill = RD, stroke = G, width = 1f)
        }
        Planet.Venus -> {
            for (i in 1..5) {
                val x = c - 34 - i * 28
                out += prim("M$x 7C${x + 7} -3 ${x + 14} 17 ${x + 21} 7", stroke = GD, width = 1.2f)
                out += prim("M${w - x} 7C${w - x - 7} -3 ${w - x - 14} 17 ${w - x - 21} 7", stroke = GD, width = 1.2f)
                out += prim(circlePath(x + 10, 7f, 1.5f), fill = GD)
                out += prim(circlePath(w - x - 10, 7f, 1.5f), fill = GD)
            }
            out += prim("M$c 13C${c - 10} 8 ${c - 9} 1 $c 4C${c + 9} 1 ${c + 10} 8 $c 13Z", fill = RD, stroke = G, width = 1f)
        }
        Planet.Saturn -> {
            val sq = "M-2.6 -2.6H2.6V2.6H-2.6Z"
            for (i in 1..9) {
                val x = c - 34 - i * 16
                val fill = if (i % 2 == 1) GD else null
                out += prim(sq, fill = fill, stroke = GD, width = 1f, rotate = 45f, dx = x, dy = 7f)
                out += prim(sq, fill = fill, stroke = GD, width = 1f, rotate = 45f, dx = w - x, dy = 7f)
            }
            out += prim(
                "M${c - 7} 0H${c + 7}M${c - 7} 14H${c + 7}M${c - 5} 0L${c + 5} 14M${c + 5} 0L${c - 5} 14",
                stroke = G, width = 1.4f,
            )
        }
    }
    return listOf(still(out))
}
