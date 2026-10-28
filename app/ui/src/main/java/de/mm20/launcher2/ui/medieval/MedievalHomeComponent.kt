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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalView
import android.util.Log
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
import androidx.compose.ui.input.pointer.PointerEventPass
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
        remember(paletteContext) { Corners.style = readCornerStyle(paletteContext) }
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
            // R1: landscape keeps the stage inside the part of this box that is really on screen
            // (the phone drew the S1 header above the top edge); portrait stays as owner-verified.
            val view = LocalView.current
            // measured once per orientation (Home is at rest when the phone turns), so page swipes that
            // move this box do not resize the stage
            var boxTop by remember(landscape) { mutableFloatStateOf(Float.NaN) }
            val windowH = dp(view.rootView.height).takeIf { it > 0f } ?: h
            val top0 = if (boxTop.isNaN()) 0f else boxTop
            val visibleTop = (-top0).coerceIn(0f, h)
            val visibleBottom = (windowH - top0).coerceIn(visibleTop, h)
            fun geoFor(l: Boolean): StageGeo =
                if (l) {
                    stageGeo(
                        true,
                        left = maxOf(navLeft, if (cutLeft > 0f) cutLeft + CameraMargin else 0f),
                        top = visibleTop,
                        right = w - maxOf(navRight, if (cutRight > 0f) cutRight + CameraMargin else 0f),
                        bottom = minOf(visibleBottom, h - bottomInset),
                    )
                } else {
                    stageGeo(false, 0f, topPad, w, h - bottomInset)
                }
            val geo = geoFor(landscape)
            if (landscape) {
                LaunchedEffect(w, h, boxTop, windowH) {
                    Log.i("medieval", "Stage: box ${w}x$h dp at y $boxTop, window h $windowH, nav L$navLeft R$navRight B$bottomInset, cut L$cutLeft R$cutRight -> scale ${geo.scale} at ${geo.x},${geo.y}")
                }
            }
            val motion = rememberMotion()
            val welcome = rememberWelcome(motion)
            val glide = rememberRotateGlide(landscape, motion, geoFor(!landscape), geo)
            val stageW = if (landscape) LandscapeStageWidth else StageWidth
            val stageH = if (landscape) LandscapeStageHeight else StageHeight
            Box(
                Modifier
                    .fillMaxSize()
                    // S7: a touch ends the welcome at once (looked at in the first pass, never consumed)
                    .pointerInput(welcome) {
                        awaitPointerEventScope {
                            while (true) {
                                val e = awaitPointerEvent(PointerEventPass.Initial)
                                if (welcome.playing && e.changes.any { it.pressed }) welcome.stop()
                            }
                        }
                    }
                    .onGloballyPositioned {
                        val y = with(density) { it.positionInWindow().y.toDp().value }
                        // a box far off screen means another launcher page is open: measure again later
                        if (boxTop.isNaN() && kotlin.math.abs(y) <= 48f) boxTop = y
                    }
            ) {
                Box(
                    Modifier
                        .offset(geo.x.dp, geo.y.dp)
                        // R1: on a window narrower or lower than the stage, do not let Compose centre it
                        .overflowTopStart()
                        .requiredSize(stageW.dp, stageH.dp)
                        .graphicsLayer {
                            scaleX = geo.scale
                            scaleY = geo.scale
                            transformOrigin = TransformOrigin(0f, 0f)
                        }
                ) {
                    MedievalStage(state, landscape, motion, welcome, glide) { commandOpen = true }
                }
            }
            AnimatedVisibility(commandOpen, enter = fadeIn(tween(120)), exit = fadeOut(tween(100))) {
                CompositionLocalProvider(LocalMotion provides motion) { CommandOverlay { commandOpen = false } }
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
    welcome: Welcome,
    glide: RotateGlide,
    onCommandOpen: () -> Unit,
) {
    val vm: MedievalHomeVM = viewModel()
    val density = LocalDensity.current
    // T9: what may move (lifecycle, screen, Battery Saver, battery, Android animations, the Animations setting)
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, 1f),
        LocalMotion provides motion,
        LocalWelcome provides welcome,
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
    Body(Sec.Oracle) { OracleOrb(); ShieldsRising(vm) }
    Body(Sec.Ledger) { Ledger(vm) }
    Body(Sec.Dispatches) { Dispatches(vm) }
    Body(Sec.Command) { CommandBar(onCommandOpen) }
    Body(Sec.Dock) { Dock() }
}

/**
 * Landscape layout D (S6, on S1's three columns): left column header, Treasury, pennants; middle orb with
 * shields, the Forecast strip and the dock; right column Ledger (4 events), command bar, Dispatches (5 rows).
 * Sections keep their portrait internals; each is moved so its portrait body y lands on its landscape rectangle,
 * with the column width in [LocalColumnWidth]. The side columns are 232 wide: sections are laid out 222 wide
 * (Treasury 205) and drawn at 232 / that width.
 */
@Composable
private fun LandscapeSections(vm: MedievalHomeVM, glide: RotateGlide, header: @Composable () -> Unit, onCommandOpen: () -> Unit) {
    @Composable
    fun Place(sec: Sec, bodyY: Float, layoutW: Float = SideLayoutWidth, content: @Composable () -> Unit) {
        val r = LandscapeRects.getValue(sec)
        val k = r.w / layoutW
        glide.Section(sec) {
            // R1: these boxes are taller than the 374 high stage (the dock box is 646); a requiredSize larger
            // than the parent is CENTRED by Compose, which pulled the dock, command bar and Dispatches up.
            // Overflow from the top-left corner instead. S6: drawn at k so a 222 wide layout fills 232.
            Box(
                Modifier
                    .at(r.x, r.y - bodyY * k)
                    .overflowTopStart()
                    .requiredSize(layoutW.dp, (r.h / k + bodyY).dp)
                    .graphicsLayer {
                        scaleX = k
                        scaleY = k
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                CompositionLocalProvider(LocalColumnWidth provides layoutW) { content() }
            }
        }
    }
    Place(Sec.Header, 0f, content = header)
    Place(Sec.Treasury, 0f, TreasuryLayoutWidth) { TreasuryPanel() }
    // the row is laid out 247 wide and drawn at PennantsLandscapeScale; its flags are taller than in portrait
    Place(Sec.Pennants, 64f * PennantsLandscapeScale, 232f) { Pennants() }
    // the orb zone (ring, orb, four shields) is drawn at 0.9 so its 325 wide span fits the 296 wide middle column
    val o = LandscapeRects.getValue(Sec.Oracle)
    glide.Section(Sec.Oracle) {
        Box(
            Modifier
                .at(o.x + (o.w - 361f * OracleScale) / 2f, o.y - 118f * OracleScale)
                .overflowTopStart()
                .requiredSize(361.dp, 340.dp)
                .graphicsLayer {
                    scaleX = OracleScale
                    scaleY = OracleScale
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        ) { OracleOrb(); ShieldsRising(vm) }
    }
    Place(Sec.Forecast, 0f, LandscapeRects.getValue(Sec.Forecast).w) { ForecastStrip(vm) }
    Place(Sec.Ledger, 330f) { Ledger(vm) }
    Place(Sec.Dispatches, 454f) { Dispatches(vm) }
    Place(Sec.Command, 404f) { CommandBar(onCommandOpen) }
    Place(Sec.Dock, 551f + BodyLift / BodyScaleF, 296f) { Dock() }
}

/** The side-column sections are laid out this wide and drawn at 232 / this; Treasury needs a narrower layout. */
private const val SideLayoutWidth = 222f
internal const val TreasuryLayoutWidth = 205f

/** Lets a requiredSize box that is larger than its parent start at the parent's top-left corner (no centring). */
internal fun Modifier.overflowTopStart(): Modifier = wrapContentSize(Alignment.TopStart, unbounded = true)

private const val OracleScale = 0.9f
private const val BodyScale = BodyScaleF
private const val BodyWidth = 361f
private const val BodyHeight = 640f + BodyLift / BodyScaleF
private const val BodyTop = HeaderTop + 120f - BodyLift

/** The four shields rise with the welcome (S7); a plain box on the whole body, so positions inside do not change. */
@Composable
private fun ShieldsRising(vm: MedievalHomeVM) {
    val welcome = LocalWelcome.current
    val density = LocalDensity.current.density
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (welcome.playing) {
                    alpha = welcome.alpha(Rise.Shields)
                    translationY = welcome.lift(Rise.Shields) * density
                }
            },
    ) { Shields(vm) }
}
