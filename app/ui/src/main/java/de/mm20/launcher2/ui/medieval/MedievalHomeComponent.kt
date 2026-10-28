package de.mm20.launcher2.ui.medieval

import de.mm20.launcher2.permissions.PermissionGroup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import de.mm20.launcher2.ui.launcher.scaffold.components.ScaffoldComponent

/** Design C5 is laid out on a 390 x 842 dp stage that is scaled to fit the screen. */
internal const val StageWidth = 390f
internal const val StageHeight = 842f

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
        val paletteContext = LocalContext.current
        remember(paletteContext) { Medieval.palette = readPalette(paletteContext) }
        var commandOpen by remember { mutableStateOf(false) }
        BoxWithConstraints(
            modifier = modifier
                .background(Medieval.Ink)
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            0f to Medieval.BgMid,
                            0.82f to Medieval.BgEdge,
                            center = Offset(size.width * 0.5f, size.height * 0.42f),
                            radius = size.width * 0.9f,
                        )
                    )
                }
                .pointerInput(Unit) {},
        ) {
            // The launcher draws edge to edge, so keep the stage clear of the system bars:
            // the navigation bar (3 buttons or gesture line) at the bottom, and any status bar
            // taller than the 20 dp the design already leaves above the header at the top.
            val density = LocalDensity.current
            val bottomInset = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
            val topInset = with(density) { WindowInsets.statusBarsIgnoringVisibility.getTop(this).toDp() }
            val topPad = (topInset - 20.dp).coerceAtLeast(0.dp)
            val freeHeight = maxHeight - topPad - bottomInset
            val scale = minOf(maxWidth.value / StageWidth, freeHeight.value / StageHeight)
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topPad, bottom = bottomInset),
            ) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .requiredSize(StageWidth.dp, StageHeight.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                ) {
                    MedievalStage(state) { commandOpen = true }
                }
            }
            AnimatedVisibility(commandOpen, enter = fadeIn(tween(120)), exit = fadeOut(tween(100))) {
                CommandOverlay { commandOpen = false }
            }
            val vm: MedievalHomeVM = viewModel()
            val notifications by vm.notifications.collectAsState()
            val hasNotificationAccess by vm.hasNotificationAccess.collectAsState()
            HideStatusBarWhileHome()
            MedievalPill(notifications, hasNotificationAccess == true)
            GrainAndVignette(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MedievalStage(state: LauncherScaffoldState, onCommandOpen: () -> Unit) {
    val vm: MedievalHomeVM = viewModel()
    val density = LocalDensity.current
    // T9: what may move (lifecycle, screen, Battery Saver, battery, Android animations, the Animations setting)
    val motion = rememberMotion()
    CompositionLocalProvider(LocalDensity provides Density(density.density, 1f), LocalMotion provides motion) {
        HeaderSection(vm.forecast, vm.weatherStatus) {
            vm.permissionsManager.requestPermission(it, PermissionGroup.Location)
        }
        // T4: everything below the header is laid out on a 361 wide body and drawn 8% larger (361 x 1.08 = 390)
        Box(
            Modifier
                .at(0f, BodyTop)
                .requiredSize(BodyWidth.dp, BodyHeight.dp)
                .graphicsLayer {
                    scaleX = BodyScale
                    scaleY = BodyScale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) {
            TreasuryPanel()
            Pennants()
            OracleOrb()
            Shields(vm)
            Ledger(vm)
            Dispatches(vm)
            CommandBar(onCommandOpen)
            Dock()
        }
    }
}

private const val BodyScale = BodyScaleF
private const val BodyWidth = 361f
private const val BodyHeight = 640f + BodyLift / BodyScaleF
private const val BodyTop = HeaderTop + 120f - BodyLift
