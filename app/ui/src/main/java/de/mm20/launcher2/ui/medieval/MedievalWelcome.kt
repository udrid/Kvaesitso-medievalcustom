package de.mm20.launcher2.ui.medieval

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * S7 "Breaking the seal": a one-shot welcome when Home appears after the phone is unlocked (owner picks
 * 2026-10-28: variants B and C together). The gold sigil is drawn round the orb ring (C) while the four dock
 * seals crack in sequence and the Home sections fade up (B); the orb kindles last as the sigil closes.
 *
 * Trigger: Motion.wakes (Home was paused while the screen was off, then resumed) + KeyguardManager.isKeyguardLocked;
 * if the keyguard is still up it waits for ACTION_USER_PRESENT (or the flag turning false). No permission.
 * Nothing here takes touches: sections only change alpha and offset, the orb and the seals stay tappable.
 * Safety: any error, timeout, tap, Home leaving the front or the motion gate closing ends it at once and
 * everything is drawn in its normal place (ms = -1 means "not playing").
 */

private const val Tag = "medieval"
private const val PrefsName = "medieval_home"
private const val KeyWelcome = "welcome"

/** Whole show, after a short hold that lets Samsung's own unlock animation finish. */
internal const val WelcomeMs = 1500
private const val HoldMs = 120L
private const val FrameMs = 40L
/** Longest we wait for the keyguard to go away before giving up for this wake. */
private const val WaitMaxMs = 60_000L

internal fun readWelcomeOn(context: Context): Boolean =
    context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).getBoolean(KeyWelcome, true)

internal fun writeWelcomeOn(context: Context, on: Boolean) {
    context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit().putBoolean(KeyWelcome, on).apply()
}

/** The parts that rise, in the order they appear. */
internal enum class Rise(val index: Int) {
    Header(0), Treasury(1), Pennants(2), Shields(3), Forecast(3), Ledger(4), Command(5), Dispatches(6), Dock(7)
}

internal fun Sec.rise(): Rise? = when (this) {
    Sec.Header -> Rise.Header
    Sec.Treasury -> Rise.Treasury
    Sec.Pennants -> Rise.Pennants
    Sec.Forecast -> Rise.Forecast
    Sec.Ledger -> Rise.Ledger
    Sec.Command -> Rise.Command
    Sec.Dispatches -> Rise.Dispatches
    // the orb and the dock seals are there from the first frame (they are the show); their own pieces rise inside
    Sec.Oracle, Sec.Dock -> null
}

private const val RiseFirstMs = 120
private const val RiseStepMs = 70
private const val RiseLen = 380
private const val RiseLiftStage = 14f
private val SealStartMs = intArrayOf(40, 170, 300, 430)
private const val SealCrackMs = 90
private const val SealSplitMs = 280
private const val SigilDrawEnd = 700
private const val KindleFrom = 650
private const val KindleTo = 1400
private const val SigilFadeFrom = 1000
private const val SigilFadeTo = 1450

/** 0..1 progress of [t] between [from] and [to]. */
internal fun seg(t: Long, from: Int, to: Int): Float = ((t - from).toFloat() / (to - from)).coerceIn(0f, 1f)
internal fun seg(p: Float, from: Float, to: Float): Float = ((p - from) / (to - from)).coerceIn(0f, 1f)
internal fun smooth(x: Float): Float = x * x * (3f - 2f * x)

/** The welcome's clock. [ms] is -1 while nothing plays; the drawing code reads it in its draw or layer block only. */
internal class Welcome {
    var ms by mutableLongStateOf(-1L)

    /** Set by a tap or by the gate closing: the running loop must leave at its next step. */
    @Volatile
    var skip = false

    val playing: Boolean get() = ms >= 0L

    fun stop() {
        skip = true
        ms = -1L
    }

    fun alpha(r: Rise): Float {
        val t = ms
        if (t < 0L) return 1f
        val from = RiseFirstMs + r.index * RiseStepMs
        return smooth(seg(t, from, from + RiseLen))
    }

    /** Stage units a part is still below its place. */
    fun lift(r: Rise): Float = if (ms < 0L) 0f else (1f - alpha(r)) * RiseLiftStage

    /** Orb darkness: -1 when not playing (the T9 wake kindle decides), else 0 (dark) .. 1 (alive). */
    fun kindle(): Float {
        val t = ms
        return if (t < 0L) -1f else smooth(seg(t, KindleFrom, KindleTo))
    }
}

internal val LocalWelcome = compositionLocalOf { Welcome() }

/** Lets a test (and nothing else) drive the welcome by hand. */
internal val LocalWelcomeOverride = compositionLocalOf<Welcome?> { null }

/** Creates the welcome and starts it on every wake that ends on Home, if the gate and the setting allow. */
@Composable
internal fun rememberWelcome(motion: Motion): Welcome {
    val context = LocalContext.current.applicationContext
    val override = LocalWelcomeOverride.current
    val welcome = override ?: remember { Welcome() }
    val motionNow = rememberUpdatedState(motion)
    // Home left the front, the screen went off, Battery Saver came on...: show everything at once
    LaunchedEffect(motion.allowed) { if (!motion.allowed) welcome.stop() }
    LaunchedEffect(motion.wakes) {
        if (override != null || motion.wakes == 0) return@LaunchedEffect
        runWelcome(context, welcome, motion.wakes) { motionNow.value.allowed }
    }
    return welcome
}

private suspend fun runWelcome(context: Context, w: Welcome, wake: Int, allowed: () -> Boolean) {
    w.stop()
    if (!readWelcomeOn(context)) {
        Log.i(Tag, "Welcome: wake $wake, setting is Off")
        return
    }
    if (!allowed()) {
        Log.i(Tag, "Welcome: wake $wake, motion gate closed")
        return
    }
    try {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val locked = km?.isKeyguardLocked ?: false
        Log.i(Tag, "Welcome: wake $wake, keyguard locked=$locked")
        if (locked && km != null && !waitForUnlock(context, km, allowed)) return
        val done = withTimeoutOrNull(WelcomeMs + HoldMs + 1000L) { play(w, allowed) }
        Log.i(Tag, if (done == null) "Welcome: timed out, shown at once" else "Welcome: finished")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(Tag, "Welcome failed, Home is shown at once", e)
    } finally {
        w.stop()
    }
}

/** True when the phone is unlocked (USER_PRESENT or the keyguard flag turning false), false on timeout or gate closing. */
private suspend fun waitForUnlock(context: Context, km: KeyguardManager, allowed: () -> Boolean): Boolean {
    val present = AtomicBoolean(false)
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            present.set(true)
        }
    }
    val registered = try {
        ContextCompat.registerReceiver(context, receiver, IntentFilter(Intent.ACTION_USER_PRESENT), ContextCompat.RECEIVER_NOT_EXPORTED)
        true
    } catch (e: Exception) {
        Log.e(Tag, "Welcome: could not listen for USER_PRESENT, using the keyguard flag only", e)
        false
    }
    try {
        val deadline = SystemClock.elapsedRealtime() + WaitMaxMs
        while (SystemClock.elapsedRealtime() < deadline) {
            if (!allowed()) {
                Log.i(Tag, "Welcome: gate closed while waiting for the unlock")
                return false
            }
            if (present.get()) {
                Log.i(Tag, "Welcome: unlocked (USER_PRESENT)")
                return true
            }
            if (!km.isKeyguardLocked) {
                Log.i(Tag, "Welcome: unlocked (keyguard flag)")
                return true
            }
            delay(200)
        }
        Log.i(Tag, "Welcome: still locked after ${WaitMaxMs / 1000} s, no welcome")
        return false
    } finally {
        if (registered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Log.e(Tag, "Welcome: could not remove the USER_PRESENT listener", e)
            }
        }
    }
}

private suspend fun play(w: Welcome, allowed: () -> Boolean): Boolean {
    w.skip = false
    // frame 0 at once (sections down, orb dark, seals whole), so Home never shows full for a moment first
    w.ms = 0L
    delay(HoldMs)
    val start = withFrameMillis { it }
    while (true) {
        val t = withFrameMillis { it } - start
        if (w.skip || !allowed() || t >= WelcomeMs) break
        w.ms = t
        delay(FrameMs)
    }
    return true
}

// ---------------------------------------------------------------------------------------------
// drawing

/** Tiny deterministic scatter in 0..1 for the sparks. */
private fun scatter(i: Int, salt: Int): Float {
    var s = (i * 7919 + salt * 104729) and 0x7fffffff
    s = (s * 1103515245 + 12345) and 0x7fffffff
    return (s % 1000) / 1000f
}

/**
 * The gold sigil round the orb ring (a square canvas of 220 units, ring centred): a closing circle with a spark
 * at its tip, eight marks that appear where it has passed, a dotted outer ring, a counter-turning inner arc,
 * then a glow on the ring and a few ember sparks when the circle closes. Draws nothing when [ms] < 0.
 */
internal fun DrawScope.welcomeSigil(ms: Long) {
    if (ms < 0L) return
    val fade = 1f - seg(ms, SigilFadeFrom, SigilFadeTo)
    val k = size.width / 220f
    val c = center
    val d = seg(ms, 60, SigilDrawEnd)
    val gold = Medieval.Gold
    val deep = Medieval.GoldDeep
    val spark = Medieval.Twinkle
    val ember = Medieval.Ember
    val r1 = 91f * k

    // glow on the ring when the circle closes
    if (ms >= SigilDrawEnd) {
        val g = 1f - seg(ms, SigilDrawEnd, 1250)
        if (g > 0f) drawCircle(ember.copy(alpha = 0.32f * g), radius = 86f * k, center = c, style = Stroke(7f * k))
    }
    if (fade <= 0f) return

    drawArc(gold.copy(alpha = fade), -90f, 360f * d, false, Offset(c.x - r1, c.y - r1), Size(2 * r1, 2 * r1), style = Stroke(1.8f * k))
    val dots = seg(ms, 260, 760)
    if (dots > 0f) {
        val r2 = 95.5f * k
        drawCircle(
            deep.copy(alpha = 0.9f * dots * fade), radius = r2, center = c,
            style = Stroke(1f * k, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * k, 5f * k))),
        )
    }
    val inner = seg(ms, 240, 900)
    if (inner > 0f) {
        val r3 = 88f * k
        drawArc(deep.copy(alpha = 0.8f * fade), -90f, -360f * inner, false, Offset(c.x - r3, c.y - r3), Size(2 * r3, 2 * r3), style = Stroke(0.8f * k))
    }
    for (i in 0 until 8) {
        val at = i / 8f + 0.02f
        if (d < at) continue
        val a = ((-90f + i * 45f) * PI / 180f).toFloat()
        val x = c.x + cos(a) * r1
        val y = c.y + sin(a) * r1
        val al = (((d - at) * 8f).coerceIn(0f, 1f)) * fade
        rotate(i * 45f, Offset(x, y)) {
            if (i % 2 == 0) {
                val p = Path().apply {
                    moveTo(x, y - 5.5f * k); lineTo(x + 4f * k, y); lineTo(x, y + 5.5f * k); lineTo(x - 4f * k, y); close()
                }
                drawPath(p, gold.copy(alpha = al))
                drawPath(p, spark.copy(alpha = al), style = Stroke(0.8f * k))
            } else {
                drawLine(gold.copy(alpha = al), Offset(x, y - 4f * k), Offset(x, y + 4f * k), 1.4f * k)
                drawLine(gold.copy(alpha = al), Offset(x - 4f * k, y), Offset(x + 4f * k, y), 1.4f * k)
            }
        }
    }
    if (d in 0.001f..0.999f) {
        val a = ((-90f + 360f * d) * PI / 180f).toFloat()
        val tip = Offset(c.x + cos(a) * r1, c.y + sin(a) * r1)
        drawCircle(ember.copy(alpha = 0.35f), radius = 9f * k, center = tip)
        drawCircle(spark, radius = 3.4f * k, center = tip)
    }
    // a few ember sparks from the heart when the circle closes
    if (ms >= SigilDrawEnd) {
        for (i in 0 until 10) {
            val u = ((ms - SigilDrawEnd) / (500f + 250f * scatter(i, 1))).coerceIn(0f, 1f)
            if (u >= 1f) continue
            val a = scatter(i, 2) * 2f * PI.toFloat()
            val v = (40f + 60f * scatter(i, 3)) * k * u
            val p = Offset(c.x + cos(a) * v, c.y + sin(a) * v + 22f * k * u * u)
            drawCircle(ember.copy(alpha = (1f - u) * (1f - u)), radius = (1f + 1.2f * scatter(i, 4)) * k, center = p)
        }
    }
}

/**
 * The two halves of dock seal [i] (a 54 unit square): they cover the seal until its turn, a gold crack runs down
 * the middle, then the halves swing apart and fall, showing the emblem. Draws nothing when [ms] < 0.
 */
internal fun DrawScope.welcomeSealHalves(i: Int, ms: Long, wax: Path) {
    if (ms < 0L) return
    val t0 = SealStartMs[i.coerceIn(0, SealStartMs.lastIndex)]
    val s = seg(ms, t0 + SealCrackMs, t0 + SealCrackMs + SealSplitMs)
    if (s >= 1f) return
    val c = seg(ms, t0, t0 + SealCrackMs)
    val k = size.width / 54f
    val alpha = 1f - seg(s, 0.5f, 1f)
    val zig = listOf(27f to 2f, 31f to 13f, 24f to 24f, 30f to 34f, 25f to 44f, 27f to 52f)

    fun half(dir: Int) {
        val poly = Path().apply {
            moveTo((if (dir < 0) -20f else 74f) * k, -20f * k)
            lineTo(27f * k, -20f * k)
            zig.forEach { lineTo(it.first * k, it.second * k) }
            lineTo(27f * k, 70f * k)
            lineTo((if (dir < 0) -20f else 74f) * k, 70f * k)
            close()
        }
        withTransform({
            translate(dir * 22f * s * k, 46f * s * s * k)
            rotate(dir * 34f * s, Offset(27f * k, 27f * k))
            clipPath(poly)
        }) {
            scale(k, Offset.Zero) {
                drawPath(wax, Medieval.Wax.copy(alpha = alpha))
                drawCircle(Medieval.WaxEdge.copy(alpha = alpha), radius = 19f, center = Offset(27f, 27f), style = Stroke(2f))
                drawCircle(Color(0x2EFFBE96).copy(alpha = 0.18f * alpha), radius = 19f, center = Offset(26.4f, 26.4f), style = Stroke(1f))
            }
        }
    }
    half(-1)
    half(1)

    // the crack: drawn from the top while it opens, gone with the halves
    val line = 1f - seg(s, 0f, 0.5f)
    if (c > 0f && line > 0f) {
        val n = (zig.size - 1) * c
        var prev = zig[0]
        for (j in 1 until zig.size) {
            val f = (n - (j - 1)).coerceIn(0f, 1f)
            if (f <= 0f) break
            val cur = zig[j]
            val end = Offset((prev.first + (cur.first - prev.first) * f) * k, (prev.second + (cur.second - prev.second) * f) * k)
            drawLine(Medieval.Twinkle.copy(alpha = line), Offset(prev.first * k, prev.second * k), end, 1.2f * k)
            prev = cur
        }
    }
    // five tiny ember chips from the split
    val u = seg(ms, t0 + SealCrackMs, t0 + SealCrackMs + 420)
    if (u in 0.001f..0.999f) {
        for (j in 0 until 5) {
            val a = scatter(j + i * 5, 7) * 2f * PI.toFloat()
            val v = (18f + 22f * scatter(j + i * 5, 8)) * k * u
            drawCircle(
                Medieval.Ember.copy(alpha = (1f - u) * (1f - u)), radius = (0.9f + scatter(j, 9)) * k,
                center = Offset(size.width / 2f + cos(a) * v, size.height / 2f + sin(a) * v + 16f * k * u * u),
            )
        }
    }
}
