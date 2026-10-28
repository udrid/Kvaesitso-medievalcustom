package de.mm20.launcher2.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.ui.medieval.medievalFrame
import de.mm20.launcher2.ui.medieval.medievalPanelBackground
import de.mm20.launcher2.ui.theme.transparency.transparency

/**
 * Deliverance (S2-screens): a charcoal gradient panel with the double gold hairline.
 * Pass a [color] to draw a plain colour instead of the gradient (highlighted result).
 */
@Composable
fun LauncherCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    backgroundOpacity: Float = MaterialTheme.transparency.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    color: Color = Color.Transparent,
    content: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .medievalPanelBackground(backgroundOpacity.coerceIn(0f, 1f))
            .then(if (backgroundOpacity > 0f) Modifier.medievalFrame() else Modifier),
        shape = shape,
        content = content,
        contentColor = MaterialTheme.colorScheme.onSurface,
        color = color,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    )
}
