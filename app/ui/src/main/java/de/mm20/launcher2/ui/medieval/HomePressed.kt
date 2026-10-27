package de.mm20.launcher2.ui.medieval

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * T15: the Android Home button pressed while Home is already showing arrives as a new
 * ACTION_MAIN / CATEGORY_HOME intent in the launcher activity. Pop-ups (shield picker, command
 * typing layer) listen here and close themselves, as they do for Back.
 */
internal object HomePressed {
    private val flow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onNewIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) flow.tryEmit(Unit)
    }

    /**
     * Runs [onHome] when Home is pressed while the activity is in front. A Home press that brings
     * the launcher back from another app (activity not resumed yet) is ignored.
     */
    @Composable
    fun Listen(onHome: () -> Unit) {
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val current by rememberUpdatedState(onHome)
        LaunchedEffect(Unit) {
            flow.collect {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) current()
            }
        }
    }
}
