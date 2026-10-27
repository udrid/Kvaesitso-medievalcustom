package de.mm20.launcher2.ui.medieval

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val Tag = "medieval"
private const val PrefsName = "medieval_home"
private const val KeyAnimations = "animations"

/** Frame rate cap for everything that is not a short touch reaction (T9 battery rules). */
internal const val AmbientFps = 10
private const val AmbientFrameMs = 1000L / AmbientFps

/**
 * The "Animations" setting (T9). Contents proposed by Claude, owner may veto:
 * Minimal = orb smoke loop (with dust twinkle and charging pulse) + one-shot reactions;
 * Full = Minimal + the light drifting round the ring; Off = nothing moves.
 */
internal enum class AnimLevel { Full, Minimal, Off }

internal fun readAnimLevel(context: Context): AnimLevel {
    val name = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).getString(KeyAnimations, null)
    return AnimLevel.entries.firstOrNull { it.name == name } ?: AnimLevel.Minimal
}

internal fun writeAnimLevel(context: Context, level: AnimLevel) {
    context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit().putString(KeyAnimations, level.name).apply()
}

/**
 * What may move on Home right now. [allowed] is false unless Home is resumed, the screen is on,
 * Battery Saver is off, the battery is above 15 % or charging, Android's animations are on and
 * the setting is not Off. [wakes] counts screen-on returns to Home (for the orb kindling).
 */
internal class Motion(
    val level: AnimLevel,
    val allowed: Boolean,
    val charging: Boolean,
    val wakes: Int,
) {
    /** The orb loop and the one-shot reactions. */
    val ambient: Boolean get() = allowed
    /** The ring light: only in Full. */
    val ringLight: Boolean get() = allowed && level == AnimLevel.Full
}

internal val LocalMotion = compositionLocalOf { Motion(AnimLevel.Off, allowed = false, charging = false, wakes = 0) }

/** Watches the lifecycle, screen, Battery Saver and battery; listeners exist only while Home is resumed. */
@Composable
internal fun rememberMotion(): Motion {
    val context = LocalContext.current.applicationContext
    val owner = LocalLifecycleOwner.current
    var level by remember { mutableStateOf(readAnimLevel(context)) }
    var resumed by remember { mutableStateOf(false) }
    var interactive by remember { mutableStateOf(true) }
    var powerSave by remember { mutableStateOf(false) }
    var battery by remember { mutableIntStateOf(100) }
    var charging by remember { mutableStateOf(false) }
    var animatorsOn by remember { mutableStateOf(true) }
    var wakes by remember { mutableIntStateOf(0) }

    DisposableEffect(owner, context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        var asleep = false
        var stop: (() -> Unit)? = null
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    level = readAnimLevel(context)
                    // "Remove animations" sets the animator duration scale to 0
                    animatorsOn = ValueAnimator.areAnimatorsEnabled()
                    powerSave = pm.isPowerSaveMode
                    interactive = pm.isInteractive
                    if (stop == null) {
                        stop = watchPower(context, pm, { powerSave = it }, { interactive = it }) { pct, plugged ->
                            battery = pct
                            charging = plugged
                        }
                    }
                    resumed = true
                    if (asleep) {
                        asleep = false
                        wakes++
                    }
                }

                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    resumed = false
                    // Home paused because the screen went off (not because an app opened)
                    if (!pm.isInteractive) asleep = true
                    stop?.invoke()
                    stop = null
                }

                else -> {}
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose {
            owner.lifecycle.removeObserver(observer)
            stop?.invoke()
            stop = null
        }
    }

    val lowBattery = battery in 0..15 && !charging
    val allowed = resumed && interactive && !powerSave && !lowBattery && animatorsOn && level != AnimLevel.Off
    return Motion(level, allowed, charging, wakes)
}

private fun watchPower(
    context: Context,
    pm: PowerManager,
    setPowerSave: (Boolean) -> Unit,
    setInteractive: (Boolean) -> Unit,
    setBattery: (Int, Boolean) -> Unit,
): () -> Unit {
    fun readBattery(intent: Intent?) {
        if (intent == null) return
        val lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        setBattery(if (lvl >= 0 && scale > 0) lvl * 100 / scale else -1, plugged)
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> readBattery(intent)
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> setPowerSave(pm.isPowerSaveMode)
                Intent.ACTION_SCREEN_OFF -> setInteractive(false)
                Intent.ACTION_SCREEN_ON -> setInteractive(true)
            }
        }
    }
    try {
        // ACTION_BATTERY_CHANGED is sticky: the current state comes back at once
        val sticky = ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        readBattery(sticky)
    } catch (e: Exception) {
        Log.e(Tag, "Motion: could not watch power and battery", e)
        return {}
    }
    return {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            Log.e(Tag, "Motion: could not stop watching power", e)
        }
    }
}

/** Milliseconds since [running] became true, advanced at most [AmbientFps] times a second; frozen otherwise. */
@Composable
internal fun rememberAmbientClock(running: Boolean, fps: Int = AmbientFps): State<Long> {
    val time = remember { mutableLongStateOf(0L) }
    if (running) {
        LaunchedEffect(Unit) {
            val start = withFrameMillis { it } - time.longValue
            while (true) {
                withFrameMillis { time.longValue = it - start }
                delay(1000L / fps)
            }
        }
    }
    return time
}

/**
 * Plays a one-shot once each time [trigger] changes (not on first show), at the ambient frame rate.
 * The value goes 0 -> 1 over [durationMs] and rests at 0. Nothing plays while motion is not allowed.
 */
@Composable
internal fun rememberOneShot(trigger: Any?, durationMs: Long, enabled: Boolean): State<Float> {
    val progress = remember { mutableFloatStateOf(0f) }
    var seen by remember { mutableStateOf(trigger) }
    LaunchedEffect(trigger) {
        if (trigger == seen) return@LaunchedEffect
        seen = trigger
        if (!enabled) return@LaunchedEffect
        try {
            val start = withFrameMillis { it }
            while (true) {
                val t = withFrameMillis { it } - start
                if (t >= durationMs) break
                progress.floatValue = t.toFloat() / durationMs
                delay(AmbientFrameMs)
            }
        } finally {
            // also when a newer trigger cancels this one, never leave it half-played
            progress.floatValue = 0f
        }
    }
    return progress
}

/** Delay between the press and opening the app, so the reaction can be seen (only while motion is allowed). */
private const val LaunchDelayMs = 120L

/** Interaction source + reaction for a tap target; pass the source to clickable() and open apps via [open]. */
internal class TapReaction(
    val source: MutableInteractionSource,
    internal val amount: Animatable<Float, AnimationVector1D>,
    private val allowed: () -> Boolean,
    private val scope: CoroutineScope,
) {
    private var pending = false

    /** Runs [action] at once, or after [LaunchDelayMs] while animations are allowed (a second tap meanwhile is ignored). */
    fun open(action: () -> Unit) {
        if (!allowed()) {
            action()
            return
        }
        if (pending) return
        pending = true
        scope.launch {
            try {
                delay(LaunchDelayMs)
                action()
            } finally {
                pending = false
            }
        }
    }
}

/**
 * A touch reaction: [press] = false for a gold flash and a 4.5 % swell (shields, orb), true for a wax seal
 * pressing in. It starts on press-down (90 ms in), stays while the finger is held and fades (160 ms) on
 * release or cancel. Nothing moves when motion is not allowed.
 * (T12: before, it started on release, in the same instant the app opened, so it was never seen.)
 */
@Composable
internal fun rememberTapReaction(): TapReaction {
    val motion = LocalMotion.current
    val allowed = rememberUpdatedState(motion.ambient)
    val scope = rememberCoroutineScope()
    val source = remember { MutableInteractionSource() }
    val amount = remember { Animatable(0f) }
    LaunchedEffect(source, motion.ambient) {
        if (!motion.ambient) {
            amount.snapTo(0f)
            return@LaunchedEffect
        }
        source.interactions.collectLatest {
            when (it) {
                is PressInteraction.Press -> amount.animateTo(1f, tween(90))
                is PressInteraction.Release, is PressInteraction.Cancel -> amount.animateTo(0f, tween(160))
            }
        }
    }
    return remember(source, amount, scope) { TapReaction(source, amount, { allowed.value }, scope) }
}

/** Draws the reaction: swell + gold flash (or press-in + shade for seals); amount 0..1. */
internal fun Modifier.tapReaction(r: TapReaction, press: Boolean = false): Modifier = this
    .graphicsLayer {
        val k = r.amount.value.coerceIn(0f, 1f)
        val s = if (press) 1f - 0.08f * k else 1f + 0.045f * k
        scaleX = s
        scaleY = s
        // the flash is painted onto the drawing only (SrcAtop), which needs its own layer while it shows
        compositingStrategy = if (k > 0f) CompositingStrategy.Offscreen else CompositingStrategy.Auto
    }
    .drawWithContent {
        drawContent()
        val k = r.amount.value.coerceIn(0f, 1f)
        if (k > 0f) {
            if (press) drawRect(Color.Black.copy(alpha = 0.22f * k), blendMode = BlendMode.SrcAtop)
            else drawRect(Color(0xFFF0C86E).copy(alpha = 0.35f * k), blendMode = BlendMode.SrcAtop)
        }
    }

/** Counts how often [value] went up while this was shown (a new notification, a higher badge count). */
@Composable
internal fun rememberRises(value: Long): Int {
    val seen = remember { longArrayOf(value, 0L) }
    if (value > seen[0]) seen[1]++
    seen[0] = value
    return seen[1].toInt()
}

/** 0 -> 1 -> 0 hump of [p] between [from] and [to] (0 outside). */
internal fun bump(p: Float, from: Float, to: Float): Float =
    if (p <= from || p >= to) 0f else kotlin.math.sin(kotlin.math.PI.toFloat() * (p - from) / (to - from))
