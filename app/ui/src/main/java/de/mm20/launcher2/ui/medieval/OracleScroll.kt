package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.oraclebridge.ScrollEntry
import de.mm20.launcher2.oraclebridge.ScrollStore
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Claude's Scroll (T25, owner pick 2026-11-23: "Top of Dispatches"): Claude's newest message from the
 * laptop sits as the first Dispatches line, marked with a small gold orb. Tap opens the whole scroll;
 * swipe dismisses the newest message.
 */
@Composable
internal fun OracleScrollRow(entry: ScrollEntry, all: List<ScrollEntry>) {
    val context = LocalContext.current
    var open by remember { mutableStateOf(false) }
    var drag by remember(entry.time) { mutableFloatStateOf(0f) }
    Row(
        Modifier
            .width((LocalColumnWidth.current - if (LocalLandscape.current) 24f else 64f).dp)
            .graphicsLayer {
                translationX = drag
                alpha = 1f - (abs(drag) / 270f).coerceIn(0f, 0.6f)
            }
            .pointerInput(entry.time) {
                detectHorizontalDragGestures(
                    onDragEnd = { if (abs(drag) > 90f) ScrollStore.dismissLatest(context) else drag = 0f },
                    onDragCancel = { drag = 0f },
                    onHorizontalDrag = { _, d -> drag += d },
                )
            }
            .clickable { open = true },
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(14.dp)) {
            drawCircle(
                Brush.radialGradient(listOf(Medieval.Glow, Medieval.palette.coinB)),
                radius = size.minDimension / 2.4f,
            )
            drawCircle(Medieval.Gold, radius = size.minDimension / 2f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.2f))
        }
        MText(entry.text.replace('\n', ' '), 13f, Medieval.Gold, Modifier.weight(1f), italic = true)
        MText(scrollTime(entry.time), 11f, Medieval.TextFaint, italic = true)
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Close") } },
            dismissButton = {
                TextButton(onClick = {
                    open = false
                    openClaude(context)
                }) { Text("Speak to the Oracle") }
            },
            title = { Text("The Oracle's scroll") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    all.asReversed().forEach { e ->
                        Column {
                            MText(scrollTime(e.time), 11f, Medieval.TextFaint, italic = true)
                            MText(e.text, 14f, Medieval.TextSoft, maxLines = 40)
                        }
                    }
                }
            },
        )
    }
}

private fun scrollTime(t: Long): String =
    Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE h:mm a"))
