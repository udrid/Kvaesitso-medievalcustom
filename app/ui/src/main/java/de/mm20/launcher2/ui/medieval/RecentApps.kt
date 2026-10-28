package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** Most apps the landscape panel can show in its one row. */
internal const val RecentShown = 5

/** Height of the panel in landscape stage units (the column is 232 wide). */
internal const val RecentPanelHeight = 62f

/** Screenshot tests put fake (package, name) pairs here (the test phone has no such apps). */
internal var RecentForTests: (() -> List<Pair<String, String>>)? = null

private fun recentLabel(context: Context, packageName: String): String? = try {
    val pm = context.packageManager
    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
} catch (e: Exception) {
    Log.e("medieval", "No label for $packageName", e)
    null
}

/** T23: landscape Home, under the pennants: the apps opened most recently from this launcher, newest first. */
@Composable
internal fun RecentApps() {
    val context = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    val apps = remember(tick) {
        RecentForTests?.invoke()
            ?: UsageCounter.recent(context, 8)
                .filter { context.packageManager.getLaunchIntentForPackage(it) != null }
                .map { it to (recentLabel(context, it) ?: "?") }
    }.take(RecentShown)
    Panel(top = 0f, height = RecentPanelHeight, ribbon = "Recently used") {
        if (apps.isEmpty()) {
            MText(
                "Open an app and it will appear here", 12f, Medieval.TextDim,
                Modifier.fillMaxWidth().padding(top = 24.dp, start = 12.dp, end = 12.dp),
                italic = true, align = TextAlign.Center,
            )
        } else {
            Row(Modifier.fillMaxWidth().padding(top = 14.dp, start = 8.dp, end = 8.dp)) {
                apps.forEach { (pkg, label) -> RecentCell(pkg, label, Modifier.weight(1f)) }
                repeat(RecentShown - apps.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentCell(pkg: String, label: String, modifier: Modifier) {
    val context = LocalContext.current
    val tap = rememberTapReaction()
    Column(
        modifier
            .tapReaction(tap)
            .combinedClickable(
                interactionSource = tap.source,
                indication = null,
                onClick = {
                    tap.open {
                        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                        if (intent != null) launchSafely(context, intent, label)
                        else Log.e("medieval", "No launch intent for $pkg")
                    }
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(32.dp)) {
                val w = size.width
                val h = size.height
                val path = Path().apply {
                    moveTo(w * .5f, h * .04f)
                    lineTo(w * .92f, h * .18f)
                    lineTo(w * .92f, h * .5f)
                    cubicTo(w * .92f, h * .78f, w * .74f, h * .92f, w * .5f, h * .98f)
                    cubicTo(w * .26f, h * .92f, w * .08f, h * .78f, w * .08f, h * .5f)
                    lineTo(w * .08f, h * .18f)
                    close()
                }
                drawPath(path, Medieval.ShieldFill)
                drawPath(path, Medieval.GoldDeep, style = Stroke(1.2.dp.toPx()))
            }
            ShieldEmblem(pkg, label)
        }
        MText(
            label.uppercase(), 6.5f, Medieval.Label, Modifier.fillMaxWidth().padding(horizontal = 1.dp),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.05f, align = TextAlign.Center,
        )
    }
}
