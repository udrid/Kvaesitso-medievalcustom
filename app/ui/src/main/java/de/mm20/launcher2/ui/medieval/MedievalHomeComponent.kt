package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import de.mm20.launcher2.ui.launcher.scaffold.components.ScaffoldComponent

/** Design C5 is laid out on a 390 x 844 dp stage that is scaled to fit the screen. */
internal const val StageWidth = 390f
internal const val StageHeight = 844f

internal object MedievalHomeComponent : ScaffoldComponent() {

    override val drawBackground: Boolean = false
    override val showSearchBar: Boolean = false

    override var isAtTop: State<Boolean?> = mutableStateOf(true)
    override var isAtBottom: State<Boolean?> = mutableStateOf(true)

    @Composable
    override fun Component(
        modifier: Modifier,
        insets: PaddingValues,
        state: LauncherScaffoldState,
    ) {
        BoxWithConstraints(
            modifier = modifier
                .background(Medieval.Ink)
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            0f to Color(0xFF26211A),
                            0.82f to Color(0xFF0D0C09),
                            center = Offset(size.width * 0.5f, size.height * 0.42f),
                            radius = size.width * 0.9f,
                        )
                    )
                }
                .pointerInput(Unit) {},
        ) {
            val scale = minOf(maxWidth.value / StageWidth, maxHeight.value / StageHeight)
            Box(
                Modifier
                    .align(Alignment.Center)
                    .requiredSize(StageWidth.dp, StageHeight.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                MedievalStage(state)
            }
            GrainAndVignette(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MedievalStage(state: LauncherScaffoldState) {
    val vm: MedievalHomeVM = viewModel()
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, 1f)) {
        HeaderSection(vm.forecast)
        StatusLine()
        TreasuryPanel()
        Pennants()
        OracleOrb()
        Shields()
    }
}
