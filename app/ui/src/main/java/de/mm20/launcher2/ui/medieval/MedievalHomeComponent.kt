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
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.LayoutDirection
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
        var commandOpen by remember { mutableStateOf(false) }
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
            // The launcher draws edge to edge, so keep the stage clear of the system bars.
            // Portrait: the navigation bar at the bottom, and any status bar taller than the 20 dp the
            // design already leaves above the header at the top.
            // Landscape (S1): the navigation bar and the camera hole sit on the short sides, so the stage
            // leaves a strip on each side that has one (plus a small margin next to the camera hole).
            val density = LocalDensity.current
            val ltr = LayoutDirection.Ltr
            val navBars = WindowInsets.navigationBars
            val cutout = WindowInsets.displayCutout
            fun dp(px: Int) = with(density) { px.toDp().value }
            val bottomInset = dp(navBars.getBottom(density))
            val topInset = dp(WindowInsets.statusBarsIgnoringVisibility.getTop(density))
            val navLeft = dp(navBars.getLeft(density, ltr))
            val navRight = dp(navBars.getRight(density, ltr))
            val cutLeft = dp(cutout.getLeft(density, ltr))
            val cutRight = dp(cutout.getRight(density, ltr))
            val topPad = (topInset - 20f).coerceAtLeast(0f)
            val landscape = maxWidth > maxHeight
            val w = maxWidth.value
            val h = maxHeight.value
            fun geoFor(l: Boolean): StageGeo =
                if (l) {
                    stageGeo(
                        true,
                        left = maxOf(navLeft, if (cutLeft > 0f) cutLeft + CameraMargin else 0f),
                        top = 0f,
                        right = w - maxOf(navRight, if (cutRight > 0f) cutRight + CameraMargin else 0f),
                        bottom = h - bottomInset,
                    )
                } else {
                    stageGeo(false, 0f, topPad, w, h - bottomInset)
                }
            val geo = geoFor(landscape)
            val motion = rememberMotion()
            val glide = rememberRotateGlide(landscape, motion, geoFor(!landscape), geo)
            val stageW = if (landscape) LandscapeStageWidth else StageWidth
            val stageH = if (landscape) LandscapeStageHeight else StageHeight
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .offset(geo.x.dp, geo.y.dp)
                        .requiredSize(stageW.dp, stageH.dp)
                        .graphicsLayer {
                            scaleX = geo.scale
                            scaleY = geo.scale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                ) {
                    MedievalStage(state, landscape, motion, glide) { commandOpen = true }
                }
            }
            AnimatedVisibility(commandOpen, enter = fadeIn(tween(120)), exit = fadeOut(tween(100))) {
                CommandOverlay { commandOpen = false }
            }
            val vm: MedievalHomeVM = viewModel()
            val notifications by vm.notifications.collectAsState()
            val hasNotificationAccess by vm.hasNotificationAccess.collectAsState()
            HideStatusBarWhileHome()
            RotationAnimationEffect()
            MedievalPill(notifications, hasNotificationAccess == true)
            GrainAndVignette(Modifier.fillMaxSize())
        }
    }
}

/** Space kept between the camera hole's cutout and the first thing drawn beside it (landscape). */
private const val CameraMargin = 6f

@Composable
private fun MedievalStage(
    state: LauncherScaffoldState,
    landscape: Boolean,
    motion: Motion,
    glide: RotateGlide,
    onCommandOpen: () -> Unit,
) {
    val vm: MedievalHomeVM = viewModel()
    val density = LocalDensity.current
    // T9: what may move (lifecycle, screen, Battery Saver, battery, Android animations, the Animations setting)
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, 1f),
        LocalMotion provides motion,
        LocalLandscape provides landscape,
    ) {
        val header: @Composable () -> Unit = {
            HeaderSection(vm.forecast, vm.weatherStatus) {
                vm.permissionsManager.requestPermission(it, PermissionGroup.Location)
            }
        }
        if (landscape) {
            LandscapeSections(vm, glide, header, onCommandOpen)
        } else {
            PortraitSections(vm, glide, header, onCommandOpen)
        }
    }
}

/** Portrait, exactly as before: everything below the header is laid out on a 361 wide body drawn 8% larger. */
@Composable
private fun PortraitSections(vm: MedievalHomeVM, glide: RotateGlide, header: @Composable () -> Unit, onCommandOpen: () -> Unit) {
    @Composable
    fun Body(sec: Sec, content: @Composable () -> Unit) = glide.Section(sec) {
        Box(
            Modifier
                .at(0f, BodyTop)
                .requiredSize(BodyWidth.dp, BodyHeight.dp)
                .graphicsLayer {
                    scaleX = BodyScale
                    scaleY = BodyScale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) { content() }
    }
    glide.Section(Sec.Header, header)
    Body(Sec.Treasury) { TreasuryPanel() }
    Body(Sec.Pennants) { Pennants() }
    Body(Sec.Oracle) { OracleOrb(); Shields(vm) }
    Body(Sec.Ledger) { Ledger(vm) }
    Body(Sec.Dispatches) { Dispatches(vm) }
    Body(Sec.Command) { CommandBar(onCommandOpen) }
    Body(Sec.Dock) { Dock() }
}

/**
 * Landscape layout A (S1): left column header, Treasury, pennants; middle orb with shields and the dock
 * under it; right column Ledger, command bar, Dispatches. Sections keep their portrait internals; each is
 * moved so its portrait body y lands on its landscape rectangle, with the column width in [LocalColumnWidth].
 */
@Composable
private fun LandscapeSections(vm: MedievalHomeVM, glide: RotateGlide, header: @Composable () -> Unit, onCommandOpen: () -> Unit) {
    @Composable
    fun Place(sec: Sec, bodyY: Float, content: @Composable () -> Unit) {
        val r = LandscapeRects.getValue(sec)
        glide.Section(sec) {
            Box(Modifier.at(r.x, r.y - bodyY).requiredSize(r.w.dp, (r.h + bodyY).dp)) {
                CompositionLocalProvider(LocalColumnWidth provides r.w) { content() }
            }
        }
    }
    Place(Sec.Header, 0f, header)
    Place(Sec.Treasury, 0f) { TreasuryPanel() }
    Place(Sec.Pennants, 64f * PennantsLandscapeScale) { Pennants() }
    // the orb zone (ring, orb, four shields) is drawn at 0.9 so its 325 wide span fits the 296 wide middle column
    val o = LandscapeRects.getValue(Sec.Oracle)
    glide.Section(Sec.Oracle) {
        Box(
            Modifier
                .at(o.x + (o.w - 361f * OracleScale) / 2f, o.y - 118f * OracleScale)
                .requiredSize(361.dp, 340.dp)
                .graphicsLayer {
                    scaleX = OracleScale
                    scaleY = OracleScale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) { OracleOrb(); Shields(vm) }
    }
    Place(Sec.Ledger, 330f) { Ledger(vm) }
    Place(Sec.Dispatches, 454f) { Dispatches(vm) }
    Place(Sec.Command, 404f) { CommandBar(onCommandOpen) }
    Place(Sec.Dock, 551f + BodyLift / BodyScaleF) { Dock() }
}

private const val OracleScale = 0.9f
private const val BodyScale = BodyScaleF
private const val BodyWidth = 361f
private const val BodyHeight = 640f + BodyLift / BodyScaleF
private const val BodyTop = HeaderTop + 120f - BodyLift
