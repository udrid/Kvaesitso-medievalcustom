package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Framed dialog: double gold line, charcoal panel, title as a ribbon on the top edge (the same panel
 * as on Home). Same parameters as Material's AlertDialog as far as the launcher uses them; files
 * only change their import. The ribbon is drawn after the frame so no line crosses it.
 */
@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        DialogPanel(confirmButton, modifier, dismissButton, icon, title, text)
    }
}

/** The dialog body (split out so screenshot tests can render it without a window). */
@Composable
internal fun DialogPanel(
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    run {
        Box(modifier = modifier.widthIn(max = 420.dp).padding(top = 9.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .medievalPanelBackground()
                    .medievalFrame()
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 16.dp),
            ) {
                if (icon != null) {
                    Box(Modifier.padding(bottom = 12.dp).align(Alignment.CenterHorizontally)) { icon() }
                }
                if (title != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineSmall,
                        LocalContentColor provides Medieval.TextBright,
                    ) { Box(Modifier.padding(bottom = 12.dp)) { title() } }
                }
                if (text != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    ) {
                        Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { text() }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
            Ribbon("Deliverance", Modifier.align(Alignment.TopStart).padding(start = 14.dp))
        }
    }
}

/** Body of a framed bottom sheet: pennant on top, then the content at full width. */
@Composable
internal fun SheetBody(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().medievalFrame(bottom = false),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PennantHandle()
        Box(Modifier.fillMaxWidth(), propagateMinConstraints = true) { content() }
    }
}
