package de.mm20.launcher2.ui.medieval

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import de.mm20.launcher2.ui.R

/** Colors and fonts of design C5 (docs/design/C5.html). */
internal object Medieval {
    val Gold = Color(0xFFC9A55A)
    val GoldDeep = Color(0xFF8C6D3A)
    val GoldDark = Color(0xFF6B5430)
    val GoldLine = Color(0xFF4A3A20)
    val Cap = Color(0xFFA8884C)
    val Text = Color(0xFFE6D9B8)
    val TextBright = Color(0xFFF2E6C6)
    val TextSoft = Color(0xFFD9CCAA)
    val TextDim = Color(0xFFA89A76)
    val TextFaint = Color(0xFF8C7A52)
    val Oxblood = Color(0xFF6A1D16)
    val OxbloodDark = Color(0xFF481510)
    val Rubric = Color(0xFFA8352B)
    val RubricSoft = Color(0xFFC0534A)
    val PanelTop = Color(0xFF1D1A14)
    val PanelBottom = Color(0xFF12100C)
    val Ink = Color(0xFF0A0907)
    val ShieldFill = Color(0xFF15120D)
    val ShieldTop = Color(0xFF3A1612)
    val Wax = Color(0xFF4E1511)
    val Ember = Color(0xFFF0A050)
    val Parchment = Color(0xFFECDFBF)
    val WeatherGrey = Color(0xFFA8987A)

    val Blackletter = FontFamily(
        Font(R.font.unifrakturcook_bold, FontWeight.Bold),
        Font(R.font.unifrakturcook_bold, FontWeight.Normal),
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
