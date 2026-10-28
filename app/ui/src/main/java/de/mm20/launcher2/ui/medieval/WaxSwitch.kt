package de.mm20.launcher2.ui.medieval

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Red wax-seal switch (owner pick 2026-10-27). Same parameters as Material's Switch as far as the
 * launcher uses them, so files only change their import. On: the seal sits right, red, gold ring.
 * Off: the seal sits left, dark, no red. The thumb slides once over 120 ms; nothing loops.
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
) {
    val x by animateDpAsState(if (checked) 25.dp else 3.dp, tween(120), label = "seal")
    val sealBrush = if (checked) {
        Brush.radialGradient(listOf(Color(0xFFC0453A), Medieval.Oxblood))
    } else {
        Brush.radialGradient(listOf(Color(0xFF3A3223), Color(0xFF17140D)))
    }
    Box(
        modifier = modifier
            .size(width = 46.dp, height = 24.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .background(Color(0xFF0F0D09), CircleShape)
            .border(1.dp, if (checked) Medieval.Gold else Medieval.GoldDark, CircleShape)
            .toggleable(
                value = checked,
                enabled = enabled && onCheckedChange != null,
                role = Role.Switch,
                onValueChange = { onCheckedChange?.invoke(it) },
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = x)
                .size(18.dp)
                .background(sealBrush, CircleShape)
                .border(1.dp, if (checked) Medieval.Gold else Medieval.GoldDark, CircleShape)
        )
    }
}
