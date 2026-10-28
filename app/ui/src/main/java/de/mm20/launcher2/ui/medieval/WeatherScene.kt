package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.weather.Forecast
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor

/**
 * The little woodcut weather scene under the clock (docs/design/T4-weather.js, palette "accentSoft",
 * owner's choice A2). 128 x 52 units, shown at 64 x 26. Moving parts are stop-motion frames.
 */
internal enum class Sky { Clear, ClearNight, Partly, Cloudy, Rain, Storm, Snow, Fog }

/** Kvaesitso weather icon -> scene. Night only changes clear and partly-cloudy skies (the design has no other night scenes). */
internal fun skyOf(f: Forecast?): Sky? {
    if (f == null) return null
    return when (f.icon) {
        Forecast.CLEAR, Forecast.EXTREME_HEAT, Forecast.EXTREME_COLD -> if (f.night) Sky.ClearNight else Sky.Clear
        Forecast.PARTLY_CLOUDY -> if (f.night) Sky.ClearNight else Sky.Partly
        Forecast.OVERCAST, Forecast.WIND -> Sky.Cloudy
        Forecast.LIGHT_RAIN, Forecast.RAIN, Forecast.HEAVY_RAIN, Forecast.SLEET, Forecast.HAIL -> Sky.Rain
        Forecast.THUNDER, Forecast.THUNDERSTORM -> Sky.Storm
        Forecast.SNOW -> Sky.Snow
        Forecast.HAZE, Forecast.FOG -> Sky.Fog
        else -> null
    }
}

/** Moon phase 0..1 (0 new, 0.5 full) from the synodic month, counted from the new moon of 6 Jan 2000 18:14 UTC. */
internal fun moonPhase(epochMs: Long): Float {
    val ref = 947182440000L
    val a = (epochMs - ref) / 86_400_000.0 / 29.530588853
    return (a - floor(a)).toFloat()
}

private val G get() = Medieval.Gold
private val GD get() = Medieval.GoldDeep
private val GDD get() = Medieval.GoldLine
private val GL get() = Medieval.GoldLight
private val RD get() = Medieval.Rubric
private val RDD get() = Medieval.Badge
private val INK get() = Medieval.Ink

// palette A2 "soft"
private val Hill1 get() = Medieval.palette.hill1
private val Hill2 get() = Medieval.palette.hill2
private val Leaf get() = Medieval.palette.leaf
private val SunFill get() = Medieval.palette.sunFill
private val SunRays get() = Medieval.palette.sunRays
private val RainBlue get() = Medieval.palette.rain
private val SnowWhite get() = Medieval.palette.snow
private val BoltOchre get() = Medieval.palette.bolt
private val BoltEdge get() = Medieval.palette.boltEdge
private val WindowGlow get() = Medieval.palette.windowGlow
private val FogGrey get() = Medieval.palette.fog

private fun hills(snow: Boolean): List<Prim> {
    val e = if (snow) GL else GD
    val e2 = if (snow) Medieval.Label else GDD
    return listOf(
        prim("M0 39C18 30 38 32 58 35S98 29 128 33V52H0Z", fill = Hill1, stroke = e2, width = 0.8f),
        prim("M34 35l3-1.4M40 35.4l3-1.2M46 35.8l3-1M64 34.6l3-1.2M70 33.8l3-1.2", stroke = GDD, width = 0.6f),
        prim("M0 46C24 40 52 44 76 42S110 40 128 44V52H0Z", fill = Hill2, stroke = e, width = 1f),
        prim("M103 38C98 42 86 43 74 45S52 49 46 52", stroke = GDD, width = 1.6f),
        prim("M56 46.5l.8-2 .8 2M62 45.6l.8-2 .8 2M90 43.4l.8-2 .8 2M8 47l.8-2 .8 2", stroke = GD, width = 0.6f),
    )
}

private fun tree(snow: Boolean): List<Prim> {
    val out = mutableListOf(
        prim("M18 45V34M18 39l-3-3M18 37l3-2.5", stroke = GD, width = 1.4f),
        prim(
            "M9 34C6 30 9 25 13 26C14 21 21 20 23 24C27 22 31 26 29 30C32 32 30 37 26 36C24 38 20 38 18 36C15 38 10 37 9 34Z",
            fill = Leaf, stroke = GD, width = 0.9f,
        ),
        prim("M12 31q2-2 4 0M19 27q2-2 4 0M21 32q2-2 4 0M14 35q1.5-1.5 3 0", stroke = GDD, width = 0.6f),
    )
    if (snow) out += prim(
        "M9.5 31C9 27 11.5 25.5 13.5 26.4C14.5 22 20.5 21 22.6 24.6C25.6 22.6 29.6 25.6 28.6 29C25 26.4 22 27.4 18.5 26.4C15 27.2 12 28.6 9.5 31Z",
        fill = GL, alpha = 0.9f,
    )
    return out
}

private fun castle(win: Color, snow: Boolean): List<Prim> {
    val out = mutableListOf(
        prim("M88 37V24H95V37Z M110 37V22H117V37Z", fill = Leaf, stroke = GD, width = 0.9f),
        prim("M87 24L91.5 14.5L96 24Z M109 22L113.5 12.5L118 22Z", fill = RDD, stroke = G, width = 0.8f),
        prim("M89.2 21.5L91.5 17M111.2 19.5L113.5 15", stroke = RD, width = 0.6f),
        prim(
            "M95 37V19H96.6V17H98.6V19H100.4V17H102.4V19H104.2V17H106.2V19H108V17H110V37Z",
            fill = Medieval.palette.leaf, stroke = G, width = 0.9f,
        ),
        prim("M96 25H109M96 31H109", stroke = GDD, width = 0.5f),
        prim("M100 37V32A2.5 2.5 0 0 1 105 32V37Z", fill = INK, stroke = GD, width = 0.7f),
        prim("M100.8 33.5H104.2M100.8 35.2H104.2", stroke = GDD, width = 0.4f),
        prim(rectPath(101.8f, 21.5f, 1.4f, 3f), fill = win),
        prim(rectPath(90.8f, 27f, 1.4f, 3f), fill = win),
        prim(rectPath(112.8f, 25.5f, 1.4f, 3f), fill = win),
    )
    if (snow) {
        out += prim(
            "M89.2 20.8L91.5 15.6L93.8 20.8C92.6 19.8 90.4 19.8 89.2 20.8Z M111.2 18.8L113.5 13.6L115.8 18.8C114.6 17.8 112.4 17.8 111.2 18.8Z",
            fill = GL,
        )
        out += prim("M95.3 18.8H110", stroke = GL, width = 1f)
    }
    return out
}

private fun banner(periodMs: Long): List<Part> {
    val pole = listOf(
        prim("M102.5 17V5.5", stroke = GL, width = 0.8f),
        prim(circlePath(102.5f, 5.2f, 0.8f), fill = G),
    )
    val flags = listOf(
        "M102.5 6.5C106 5 109.5 8 113.5 6.5L112 9.3L113.5 12C109.5 13.2 106 10.5 102.5 12Z",
        "M102.5 6.5C106 8 109.5 5.2 113.5 7.2L111.8 9.6L113.5 12.2C109.5 10.6 106 13 102.5 12Z",
        "M102.5 6.5C105.5 6 110.5 6.2 113 7.6L111.6 10L112.8 12.6C109.5 11.6 105.5 12.2 102.5 12Z",
    ).map {
        listOf(
            prim(it, fill = RD, stroke = G, width = 0.6f),
            prim("M105.5 8.6l1.4 1.4M106.9 8.6l-1.4 1.4", stroke = GL, width = 0.5f),
        )
    }
    return listOf(still(pole), anim(periodMs, flags))
}

private fun land(win: Color = INK, snow: Boolean = false, flagMs: Long = 900L): List<Part> =
    listOf(still(hills(snow) + tree(snow) + castle(win, snow))) + banner(flagMs)

private fun sun(x: Float, y: Float): List<Part> {
    fun rays(rot: Float, sc: Float) = (0 until 16).map { i ->
        if (i % 2 == 1) prim(
            "M0 -8.6Q1.3 -10 0 -11.2Q-1.3 -12.4 0 -13.4", stroke = SunRays, width = 0.8f * sc,
            scale = sc, rotate = i * 22.5f + rot, dx = x, dy = y,
        )
        else prim("M-1.4 -8.6L0 -14.2L1.4 -8.6Z", fill = SunRays, scale = sc, rotate = i * 22.5f + rot, dx = x, dy = y)
    }
    val disc = listOf(
        prim(circlePath(0f, 0f, 7.6f), fill = SunFill, stroke = GL, width = 1f, dx = x, dy = y),
        prim(circlePath(0f, 0f, 6.1f), stroke = Medieval.palette.coinB, width = 0.5f, dx = x, dy = y),
        prim(
            "M-3.6 -1.4q1.1-1.1 2.2 0M1.4 -1.4q1.1-1.1 2.2 0M0 -.8V1.6H-.9M-2.4 3.1Q0 4.9 2.4 3.1",
            stroke = Medieval.palette.coinInk, width = 0.7f, dx = x, dy = y,
        ),
        prim(circlePath(-3.6f, 2f, 0.9f), fill = RD, alpha = 0.55f, dx = x, dy = y),
        prim(circlePath(3.6f, 2f, 0.9f), fill = RD, alpha = 0.55f, dx = x, dy = y),
    )
    return listOf(anim(1200L, listOf(rays(0f, 1f), rays(4f, 1.05f), rays(8f, 0.97f))), still(disc))
}

private fun faintSun(x: Float, y: Float) = still(
    listOf(
        prim(circlePath(x, y, 7.5f), fill = GL, alpha = 0.22f),
        prim(circlePath(x, y, 7.5f), stroke = GL, width = 0.6f, alpha = 0.35f),
    ),
)

/** Moon in its real phase (northern sky: a waxing moon is lit on the right). */
internal fun moonPrims(x: Float, y: Float, p: Float): List<Prim> {
    val r = 8f
    val rx = abs(cos(2 * PI * p)).toFloat() * r
    val wax = p < 0.5f
    val q = if (wax) p else 1f - p
    val out = mutableListOf(prim(circlePath(x, y, r), fill = Medieval.palette.hill1, stroke = GD, width = 0.6f, alpha = 0.9f))
    if (q > 0.02f) out += prim(
        "M0 -${r}A$r $r 0 0 1 0 ${r}A${"%.2f".format(java.util.Locale.ROOT, rx)} $r 0 0 ${if (q < 0.25f) 0 else 1} 0 -${r}Z",
        fill = GL, stroke = G, width = 0.7f, scale = 1f, scaleX = if (wax) 1f else -1f, dx = x, dy = y,
    )
    out += prim(circlePath(x + 2.6f, y - 2.4f, 1.3f), fill = GD, alpha = 0.35f)
    out += prim(circlePath(x - 2.2f, y + 2.8f, 1f), fill = GD, alpha = 0.35f)
    out += prim(circlePath(x + 3.4f, y + 3.2f, 0.8f), fill = GD, alpha = 0.35f)
    if (q > 0.44f) out += prim("M-3.4 -1.2q1-1 2 0M1.4 -1.2q1-1 2 0M-2.2 2.8Q0 4.2 2.2 2.8", stroke = GD, width = 0.6f, dx = x, dy = y)
    return out
}

private fun star(x: Float, y: Float, r: Float) =
    prim("M0 -${r}Q0 0 $r 0Q0 0 0 ${r}Q0 0 -$r 0Q0 0 0 -${r}Z", fill = GL, dx = x, dy = y)

private fun cloud(x: Float, y: Float, s: Float, fill: Color, shade: Boolean = true): List<Prim> {
    val out = mutableListOf(
        prim(
            "M3 16C-1 16 -1 10.4 3.4 10.2C2.4 5 8.4 3 11.4 6.2C13.4 0 22.4 -.4 24.4 5C27.6 2 33.6 3.4 33.4 8.4C38.6 7.6 40.6 13.4 37 16Z",
            fill = fill, stroke = G, width = 1f, scale = s, dx = x, dy = y,
        ),
        prim(
            "M9 13.4C9 10.4 13 10 13.6 12.4M20.6 9.6C20.4 6.4 25.6 6 26 9M29.6 13.4C30 11 33.6 11.2 33.4 13.4",
            stroke = GD, width = 0.7f, scale = s, dx = x, dy = y,
        ),
    )
    if (shade) out += prim("M5 14.6H13M16 14.6H28M30 14.6H35M8 12.6H10M24 12.6H27", stroke = GDD, width = 0.6f, scale = s, dx = x, dy = y)
    return out
}

/** A cloud that drifts dx units and back in four steps. */
private fun drifting(periodMs: Long, dx: Float, make: (Float) -> List<Prim>) =
    anim(periodMs, listOf(0f, dx / 2, dx, dx / 2).map(make))

private fun birds(): Part = anim(1600L, (0 until 4).map { k ->
    val up = k % 2 == 0
    val x = k * 1.6f
    fun b(bx: Float, by: Float) = if (up) prim("M$bx ${by}q1.6-1.8 3.2 0q1.6-1.8 3.2 0", stroke = GL, width = 0.7f)
    else prim("M$bx ${by - 1}q1.6 1.6 3.2 1q1.6.6 3.2-1", stroke = GL, width = 0.7f)
    listOf(b(58 + x, 11f), b(68 + x, 7.5f))
})

private fun rain(n: Int, slant: Float, top: Int, bot: Int): Part = anim(600L, (0 until 3).map { k ->
    (0 until n).map { j ->
        val x = 28f + j * (44f / n) + k * 1.4f
        val y = top + ((j * 7 + k * 5) % (bot - top - 5))
        prim("M${"%.1f".format(java.util.Locale.ROOT, x)} ${y}l$slant 4.5", stroke = RainBlue, width = 1f)
    }
})

private fun ripples(): Part = anim(
    900L,
    listOf(
        listOf(prim(ellipsePath(44f, 48f, 2f, 0.6f), stroke = RainBlue, width = 0.5f)),
        listOf(
            prim(ellipsePath(60f, 46.6f, 2f, 0.6f), stroke = RainBlue, width = 0.5f),
            prim(ellipsePath(44f, 48f, 3.2f, 0.9f), stroke = RainBlue, width = 0.4f, alpha = 0.6f),
        ),
        listOf(
            prim(ellipsePath(34f, 48.6f, 2f, 0.6f), stroke = RainBlue, width = 0.5f),
            prim(ellipsePath(60f, 46.6f, 3.2f, 0.9f), stroke = RainBlue, width = 0.4f, alpha = 0.6f),
        ),
    ),
)

private fun snowfall(): Part = anim(1600L, (0 until 4).map { k ->
    (0 until 11).map { j ->
        val x = 8f + j * 10.5f + ((j * 3 + k) % 3) - 1
        val y = 16f + ((j * 9 + k * 4) % 26)
        prim("M0 -1.4V1.4M-1.2 -.7L1.2 .7M-1.2 .7L1.2 -.7", stroke = SnowWhite, width = 0.55f, dx = x, dy = y)
    }
})

private fun fog(): List<Part> = listOf(
    still(listOf(prim(rectPath(0f, 24f, 128f, 28f), fill = Color(0xFF2A2418), alpha = 0.35f))),
    anim(1800L, (0 until 3).map { k ->
        val o = k * 3f
        listOf(
            prim("M${-12 + o} 30C8 27 28 33 48 30S88 27 140 30", stroke = FogGrey, width = 3.2f, alpha = 0.32f),
            prim("M${-6 - o} 38C14 35 36 41 58 38S100 35 140 38", stroke = FogGrey, width = 3.6f, alpha = 0.38f),
            prim("M${-10 + o} 45C10 42 34 48 56 45S98 42 140 45", stroke = FogGrey, width = 3f, alpha = 0.3f),
            prim("M${4 + o} 34H40M${60 - o} 41H96M${20 + o} 48H70", stroke = GL, width = 0.6f, alpha = 0.45f),
        )
    }),
)

private fun bolt() = prim("M52 20L46 30.5H51L44.5 42L57 27.5H52L56.5 20Z", fill = BoltOchre, stroke = BoltEdge, width = 0.8f)

internal fun weatherParts(sky: Sky, phase: Float): List<Part> = when (sky) {
    Sky.Clear -> sun(36f, 15f) + birds() + land()
    Sky.ClearNight -> listOf(
        still(moonPrims(36f, 15f, phase)),
        anim(
            1400L,
            listOf(
                listOf(star(12f, 8f, 1.8f), star(52f, 6f, 1.2f), star(70f, 14f, 1.6f), star(24f, 22f, 1f)),
                listOf(star(12f, 8f, 1f), star(52f, 6f, 2f), star(70f, 14f, 1f), star(24f, 22f, 1.6f)),
            ),
        ),
        still(listOf(star(80f, 5f, 0.9f), star(62f, 20f, 0.8f))),
    ) + land(win = WindowGlow) +
        anim(1200L, listOf(listOf(prim(rectPath(101.8f, 21.5f, 1.4f, 3f), fill = Color(0xFFFFC070))), emptyList()))
    Sky.Partly -> sun(30f, 14f) + drifting(2400L, 4f) { o -> cloud(34f + o, 10f, 0.9f, Leaf) } + land()
    Sky.Cloudy -> listOf(
        drifting(2800L, 4f) { o -> cloud(6f + o, 3f, 1f, Medieval.palette.leaf) },
        drifting(2800L, -4f) { o -> cloud(46f + o, 11f, 0.85f, Leaf) },
    ) + land()
    Sky.Rain -> listOf(still(cloud(20f, 2f, 1.25f, Medieval.palette.hill1)), rain(9, -1.6f, 22, 44)) + land() + ripples()
    Sky.Storm -> listOf(
        anim(2400L, listOf(emptyList(), emptyList(), listOf(prim(rectPath(0f, 0f, 128f, 40f), fill = GL, alpha = 0.09f)), emptyList(), emptyList(), emptyList())),
        still(cloud(18f, 0f, 1.35f, Medieval.palette.hill2) + cloud(54f, 5f, 0.8f, Medieval.palette.hill1)),
        rain(12, -2.4f, 22, 44),
        anim(2400L, listOf(emptyList(), emptyList(), listOf(bolt()), emptyList(), listOf(bolt()), emptyList())),
    ) + land(flagMs = 450L)
    Sky.Snow -> listOf(still(cloud(22f, 2f, 1.2f, Leaf))) + land(snow = true) + snowfall()
    Sky.Fog -> listOf(faintSun(40f, 14f)) + land() + fog()
}

/** 64 x 26 animated scene for the current weather; nothing when there is no forecast. */
@Composable
internal fun WeatherScene(forecast: Forecast?, nowMs: Long, modifier: Modifier = Modifier) {
    val sky = skyOf(forecast) ?: return
    // phase in 1/100 steps so the drawing is rebuilt a few times a day, not every tick
    val phaseKey = (moonPhase(nowMs) * 100).toInt()
    val parts = remember(sky, phaseKey, Medieval.palette) { weatherParts(sky, phaseKey / 100f) }
    VectorCanvas(parts, 128f, modifier.size(74.dp, 30.dp), animated = true)
}
