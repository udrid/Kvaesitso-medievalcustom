package de.mm20.launcher2.ui.medieval

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * S2-screens: the Deliverance look for every launcher screen that is not Home.
 * Colours, sharp shapes and fonts are handed to MaterialTheme in LauncherTheme, so every
 * screen follows. Frames, ribbons, the pennant and the wax-seal switch are drawn here.
 * All static drawing: nothing in this file animates by itself (motion rules, DESIGN-SYSTEM.md).
 */
internal object Deliverance {
    /**
     * Material colours of the active palette (S4). Reading it reads `Medieval.palette`, so LauncherTheme
     * recomposes and every launcher screen recolours when the colour scheme changes.
     */
    val colorScheme: ColorScheme
        get() {
            val p = Medieval.palette
            return schemeCache?.takeIf { it.first === p }?.second
                ?: schemeOf().also { schemeCache = p to it }
        }

    private var schemeCache: Pair<Palette, ColorScheme>? = null

    private fun schemeOf(): ColorScheme = darkColorScheme(
        primary = Medieval.Gold,
        onPrimary = Medieval.Ink,
        primaryContainer = Medieval.Oxblood,
        onPrimaryContainer = Medieval.TextBright,
        inversePrimary = Medieval.GoldDeep,
        secondary = Medieval.Cap,
        onSecondary = Medieval.Ink,
        secondaryContainer = Medieval.GoldLine,
        onSecondaryContainer = Medieval.Text,
        tertiary = Medieval.RubricSoft,
        onTertiary = Medieval.Ink,
        tertiaryContainer = Medieval.OxbloodDark,
        onTertiaryContainer = Medieval.Text,
        background = Medieval.Ink,
        onBackground = Medieval.Text,
        surface = Medieval.PanelBottom,
        onSurface = Medieval.Text,
        surfaceVariant = Medieval.PanelTop,
        onSurfaceVariant = Medieval.TextDim,
        surfaceTint = Medieval.Gold,
        inverseSurface = Medieval.PanelTop,
        inverseOnSurface = Medieval.Text,
        error = Medieval.RubricSoft,
        onError = Medieval.Ink,
        errorContainer = Medieval.Oxblood,
        onErrorContainer = Medieval.TextBright,
        outline = Medieval.GoldDark,
        outlineVariant = Medieval.GoldLine,
        scrim = Color.Black,
        surfaceBright = Medieval.PanelTop,
        surfaceDim = Medieval.Ink,
        surfaceContainerLowest = Medieval.Ink,
        // the original Gold and Oxblood values (0F0D0A, 14120E, 1A1710, 221E15) mixed from the palette's ground
        surfaceContainerLow = lerp(Medieval.Ink, Medieval.PanelBottom, 0.6f),
        surfaceContainer = lerp(Medieval.PanelBottom, Medieval.PanelTop, 0.2f),
        surfaceContainerHigh = lerp(Medieval.PanelBottom, Medieval.PanelTop, 0.75f),
        surfaceContainerHighest = lerp(Medieval.PanelTop, Medieval.GoldLine, 0.12f),
    )

    /** Owner pick 2026-10-27: sharp corners everywhere (no cut corners, no rounding). */
    val shapes: Shapes = RoundedCornerShape(0.dp).let {
        Shapes(
            extraSmall = it, small = it, medium = it, large = it,
            largeIncreased = it, extraLarge = it, extraLargeIncreased = it, extraExtraLarge = it,
        )
    }

    private fun TextStyle.blackletter() = copy(
        fontFamily = Medieval.Blackletter,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.sp,
    )

    private fun TextStyle.body(size: Int) = copy(
        fontFamily = Medieval.FellEnglish,
        fontWeight = FontWeight.Normal,
        fontSize = size.sp,
        lineHeight = (size * 1.35f).sp,
        letterSpacing = 0.2.sp,
    )

    private fun TextStyle.caps(size: Int, spacing: Float) = copy(
        fontFamily = Medieval.Cinzel,
        fontWeight = FontWeight.Bold,
        fontSize = size.sp,
        lineHeight = (size * 1.5f).sp,
        letterSpacing = spacing.sp,
    )

    val typography: Typography = Typography().let { t ->
        t.copy(
            displayLarge = t.displayLarge.blackletter(),
            displayMedium = t.displayMedium.blackletter(),
            displaySmall = t.displaySmall.blackletter(),
            headlineLarge = t.headlineLarge.blackletter(),
            headlineMedium = t.headlineMedium.blackletter(),
            headlineSmall = t.headlineSmall.blackletter(),
            titleLarge = t.titleLarge.blackletter(),
            titleMedium = t.titleMedium.body(16),
            titleSmall = t.titleSmall.caps(11, 1.6f),
            bodyLarge = t.bodyLarge.body(16),
            bodyMedium = t.bodyMedium.body(15),
            bodySmall = t.bodySmall.body(13),
            labelLarge = t.labelLarge.caps(11, 1.4f),
            labelMedium = t.labelMedium.caps(10, 1.4f),
            labelSmall = t.labelSmall.caps(9, 1.2f),
            displayLargeEmphasized = t.displayLargeEmphasized.blackletter(),
            displayMediumEmphasized = t.displayMediumEmphasized.blackletter(),
            displaySmallEmphasized = t.displaySmallEmphasized.blackletter(),
            headlineLargeEmphasized = t.headlineLargeEmphasized.blackletter(),
            headlineMediumEmphasized = t.headlineMediumEmphasized.blackletter(),
            headlineSmallEmphasized = t.headlineSmallEmphasized.blackletter(),
            titleLargeEmphasized = t.titleLargeEmphasized.blackletter(),
            titleMediumEmphasized = t.titleMediumEmphasized.body(16),
            titleSmallEmphasized = t.titleSmallEmphasized.caps(11, 1.6f),
            bodyLargeEmphasized = t.bodyLargeEmphasized.body(16),
            bodyMediumEmphasized = t.bodyMediumEmphasized.body(15),
            bodySmallEmphasized = t.bodySmallEmphasized.body(13),
            labelLargeEmphasized = t.labelLargeEmphasized.caps(11, 1.4f),
            labelMediumEmphasized = t.labelMediumEmphasized.caps(10, 1.4f),
            labelSmallEmphasized = t.labelSmallEmphasized.caps(9, 1.2f),
        )
    }
}

/** Vertical charcoal gradient of every panel (C5). */
internal fun Modifier.medievalPanelBackground(alpha: Float = 1f): Modifier = background(
    Brush.verticalGradient(
        listOf(Medieval.PanelTop.copy(alpha = alpha), Medieval.PanelBottom.copy(alpha = alpha))
    )
)

/**
 * The double gold hairline (outer GoldDark, inner GoldLine 3 dp inside). `top` / `bottom` switch
 * the horizontal lines off so a stack of list items reads as ONE framed panel; the side lines then
 * run through the padding between items. [topInset] / [bottomInset] are padding the item
 * adds above / below its drawn body, so the lines meet the visible card.
 * Draw this AFTER the content, and draw a Ribbon after it, so lines never run over the ribbon.
 */
internal fun Modifier.medievalFrame(
    top: Boolean = true,
    bottom: Boolean = true,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp,
): Modifier = drawWithContent {
    drawContent()
    drawFrameLines(top, bottom, topInset.toPx(), bottomInset.toPx())
}

private fun DrawScope.drawFrameLines(top: Boolean, bottom: Boolean, topInset: Float, bottomInset: Float) {
    val w = 1.dp.toPx()
    val gap = 3.dp.toPx()
    fun lines(inset: Float, color: Color) {
        val x0 = inset + w / 2
        val x1 = size.width - inset - w / 2
        val y0 = if (top) topInset + inset + w / 2 else 0f
        val y1 = if (bottom) size.height - bottomInset - inset - w / 2 else size.height
        drawLine(color, Offset(x0, y0), Offset(x0, y1), w)
        drawLine(color, Offset(x1, y0), Offset(x1, y1), w)
        if (top) drawLine(color, Offset(x0 - w / 2, y0), Offset(x1 + w / 2, y0), w)
        if (bottom) drawLine(color, Offset(x0 - w / 2, y1), Offset(x1 + w / 2, y1), w)
    }
    lines(0f, Medieval.GoldDark)
    lines(gap, Medieval.GoldLine)
}

/** Swallow-tailed ribbon end. */
private val DeliveranceRibbonShape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density,
    ): androidx.compose.ui.graphics.Outline {
        val tail = with(density) { 7.dp.toPx() }
        return androidx.compose.ui.graphics.Outline.Generic(Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - tail, size.height / 2f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        })
    }
}

/**
 * Red heading ribbon with a gold top line and a swallow tail. It is opaque and must be placed
 * on top of (after) the panel frame so no gold line crosses it.
 */
@Composable
internal fun Ribbon(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(17.dp)
            .widthIn(max = 280.dp)
            .clip(DeliveranceRibbonShape)
            .background(Brush.verticalGradient(listOf(Medieval.Oxblood, Medieval.OxbloodDark)))
            .drawBehind { drawLine(Medieval.Gold, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx()) }
            .padding(start = 11.dp, end = 17.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = Medieval.Cinzel,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp,
                letterSpacing = 2.sp,
                color = Medieval.RibbonText,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Pennant that replaces the bottom sheet grab bar. */
@Composable
internal fun PennantHandle(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .semantics { contentDescription = "Drag handle" }
            .width(60.dp)
            .height(12.dp)
            .drawBehind {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.4f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height * 0.4f)
                    close()
                }
                drawPath(path, Brush.verticalGradient(listOf(Medieval.Oxblood, Medieval.OxbloodDark)))
                drawLine(Medieval.Gold, Offset(0f, 0.5f), Offset(size.width, 0.5f), 1.dp.toPx())
            }
    )
}

/** Thin rule under screen titles: gold fading to nothing (as on Home). */
internal fun Modifier.goldRule(): Modifier = fillMaxWidth()
    .height(1.dp)
    .background(Brush.horizontalGradient(listOf(Medieval.GoldDark, Color.Transparent)))
