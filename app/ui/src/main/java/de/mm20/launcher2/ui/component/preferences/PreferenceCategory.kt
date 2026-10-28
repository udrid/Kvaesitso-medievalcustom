package de.mm20.launcher2.ui.component.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.medieval.Ribbon
import de.mm20.launcher2.ui.medieval.medievalFrame
import de.mm20.launcher2.ui.medieval.medievalPanelBackground


/**
 * Deliverance (S2-screens): a framed panel with the title as a red ribbon on its top edge.
 * The ribbon is placed after the panel so the gold lines never cross it.
 */
@Composable
fun PreferenceCategory(
    title: String? = null,
    iconPadding: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (title != null) 8.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .medievalPanelBackground()
                .medievalFrame()
                .padding(top = if (title != null) 12.dp else 0.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            content()
        }
        if (title != null) {
            Ribbon(title, Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = (-8).dp))
        }
    }
}
