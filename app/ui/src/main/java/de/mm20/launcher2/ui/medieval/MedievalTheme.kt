package de.mm20.launcher2.ui.medieval

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import de.mm20.launcher2.ui.R

/**
 * Colors and fonts of design C5 (docs/design/C5.html). The colours come from the active [Palette] (S4):
 * the palette is Compose state, so everything that reads `Medieval.X` while composing or drawing updates on a change.
 */
internal object Medieval {
    var palette: Palette by mutableStateOf(Palettes.GoldOxblood)

    val Gold get() = palette.gold
    val GoldDeep get() = palette.goldDeep
    val GoldDark get() = palette.goldDark
    val GoldLine get() = palette.goldLine
    val Cap get() = palette.cap
    val Text get() = palette.text
    val TextBright get() = palette.textBright
    val TextSoft get() = palette.textSoft
    val TextDim get() = palette.textDim
    val TextFaint get() = palette.textFaint
    val Oxblood get() = palette.oxblood
    val OxbloodDark get() = palette.oxbloodDark
    val Rubric get() = palette.rubric
    val RubricSoft get() = palette.rubricSoft
    val PanelTop get() = palette.panelTop
    val PanelBottom get() = palette.panelBottom
    val Ink get() = palette.ink
    val ShieldFill get() = palette.shieldFill
    val ShieldTop get() = palette.shieldTop
    val Wax get() = palette.wax
    val WaxEdge get() = palette.waxEdge
    val Ember get() = palette.ember
    val Parchment get() = palette.parchment
    val WeatherGrey get() = palette.weatherGrey
    val Label get() = palette.label
    val RibbonText get() = palette.ribbonText
    val GoldLight get() = palette.goldLight
    val SummonDark get() = palette.summonDark
    val BgMid get() = palette.bgMid
    val BgEdge get() = palette.bgEdge
    val Glow get() = palette.glow
    val Heart get() = palette.heart
    val Twinkle get() = palette.twinkle
    val Halo get() = palette.halo
    val Gem get() = palette.gem
    val Badge get() = palette.badge

    val Blackletter = FontFamily(
        Font(R.font.unifrakturcook_bold, FontWeight.Bold),
        Font(R.font.unifrakturcook_bold, FontWeight.Normal),
    )

    /** T4 header: weekday name and its handpainted first letter. */
    val Gothic = FontFamily(
        Font(R.font.unifrakturmaguntia_book, FontWeight.Normal),
    )

    val Cinzel = FontFamily(
        Font(
            R.font.cinzel_variable,
            FontWeight.Medium,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.cinzel_variable,
            FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(500)),
        ),
        Font(
            R.font.cinzel_variable,
            FontWeight.Bold,
            variationSettings = FontVariation.Settings(FontVariation.weight(700)),
        ),
    )

    val FellEnglish = FontFamily(
        Font(R.font.imfellenglish_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.imfellenglish_italic, FontWeight.Normal, FontStyle.Italic),
    )
}
