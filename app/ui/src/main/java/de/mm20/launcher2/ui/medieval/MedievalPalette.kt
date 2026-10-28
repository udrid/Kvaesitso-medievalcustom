package de.mm20.launcher2.ui.medieval

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import de.mm20.launcher2.ui.R

/**
 * S4 colour scheme: every colour of the Deliverance look in one place. `Medieval.X` reads the active palette,
 * so a change in settings recolours Home and the other medieval screens at once (docs/design/S4-colour-scheme.html).
 */
internal class Palette(
    val id: String,
    val title: String,
    val gold: Color,
    val goldDeep: Color,
    val goldDark: Color,
    val goldLine: Color,
    val cap: Color,
    val text: Color,
    val textBright: Color,
    val textSoft: Color,
    val textDim: Color,
    val textFaint: Color,
    val oxblood: Color,
    val oxbloodDark: Color,
    val rubric: Color,
    val rubricSoft: Color,
    val panelTop: Color,
    val panelBottom: Color,
    val ink: Color,
    val shieldFill: Color,
    val shieldTop: Color,
    val wax: Color,
    val waxEdge: Color,
    val ember: Color,
    val parchment: Color,
    val weatherGrey: Color,
    val label: Color,
    val ribbonText: Color,
    val goldLight: Color,
    val summonDark: Color,
    val bgMid: Color,
    val bgEdge: Color,
    val glow: Color,
    val heart: Color,
    val twinkle: Color,
    val halo: Color,
    val coinA: Color,
    val coinB: Color,
    val coinRim: Color,
    val coinInk: Color,
    val barA: Color,
    val barB: Color,
    val pennantA: Color,
    val pennantB: Color,
    val pennantC: Color,
    val capShadow: Color,
    val capA: Color,
    val capB: Color,
    val capC: Color,
    val capD: Color,
    val shimmer: Color,
    val press: Color,
    val badge: Color,
    val feather: Color,
    val gem: Color,
    val avatarA: Color,
    val avatarB: Color,
    val hill1: Color,
    val hill2: Color,
    val leaf: Color,
    val sunFill: Color,
    val sunRays: Color,
    val rain: Color,
    val snow: Color,
    val bolt: Color,
    val boltEdge: Color,
    val windowGlow: Color,
    val fog: Color,
    val orbSmoke: Int,
    val orbGlass: Int,
    val orbSetting: Int,
    val swatches: List<Color>,
)

private fun c(hex: Long) = Color(0xFF000000 or hex)

/**
 * A palette from its main colours; the many small roles are mixed from those so a new scheme stays short.
 * Weather colours keep their natural look (blue rain, white snow) but sit in the palette's ground tones.
 */
private fun scheme(
    id: String, title: String,
    gold: Long, goldDeep: Long, goldDark: Long, goldLine: Long, cap: Long,
    text: Long, textBright: Long, textSoft: Long, textDim: Long, textFaint: Long, label: Long,
    ox: Long, oxDark: Long, rub: Long, rubSoft: Long,
    panelTop: Long, panelBottom: Long, ink: Long, bgMid: Long, bgEdge: Long,
    shieldTop: Long, wax: Long, ember: Long, parch: Long, weather: Long,
    glow: Long, orbHeart: Long, light: Long,
    smoke: Int, glass: Int, setting: Int,
): Palette {
    val g = c(gold)
    val o = c(ox)
    val i = c(ink)
    val pt = c(panelTop)
    return Palette(
        id, title, g, c(goldDeep), c(goldDark), c(goldLine), c(cap),
        c(text), c(textBright), c(textSoft), c(textDim), c(textFaint),
        o, c(oxDark), c(rub), c(rubSoft), pt, c(panelBottom), i,
        shieldFill = lerp(i, pt, 0.55f), shieldTop = c(shieldTop), wax = c(wax),
        waxEdge = lerp(c(wax), Color.Black, 0.65f),
        ember = c(ember), parchment = c(parch), weatherGrey = c(weather),
        label = c(label), ribbonText = lerp(c(parch), Color.White, 0.1f), goldLight = c(light),
        summonDark = lerp(c(shieldTop), Color.Black, 0.55f),
        bgMid = c(bgMid), bgEdge = c(bgEdge),
        glow = c(glow), heart = c(orbHeart), twinkle = lerp(c(orbHeart), Color.White, 0.7f),
        halo = c(rub).copy(alpha = 0.42f),
        coinA = lerp(g, Color.White, 0.2f), coinB = c(goldDeep), coinRim = c(goldDark), coinInk = lerp(c(goldLine), Color.Black, 0.5f),
        barA = lerp(o, Color.Black, 0.4f), barB = c(ember),
        pennantA = c(goldDeep), pennantB = c(goldDark), pennantC = o,
        capShadow = lerp(c(rub), Color.Black, 0.88f), capA = c(rubSoft), capB = c(rub),
        capC = lerp(c(rub), Color.Black, 0.55f), capD = lerp(c(rub), Color.Black, 0.2f),
        shimmer = lerp(g, Color.White, 0.3f), press = c(ember), badge = o,
        feather = lerp(i, g, 0.12f), gem = lerp(c(rub), Color.Black, 0.2f),
        avatarA = lerp(pt, g, 0.1f), avatarB = lerp(c(panelBottom), Color.Black, 0.2f),
        hill1 = lerp(c(panelBottom), c(panelTop), 0.2f), hill2 = lerp(i, c(panelBottom), 0.5f), leaf = pt,
        sunFill = lerp(g, c(goldDeep), 0.35f), sunRays = lerp(g, c(goldDeep), 0.2f),
        rain = lerp(Color(0xFF8AA4C4), g, 0.15f), snow = lerp(c(parch), Color.White, 0.2f),
        bolt = g, boltEdge = lerp(g, Color.White, 0.45f), windowGlow = c(ember), fog = c(textDim),
        orbSmoke = smoke, orbGlass = glass, orbSetting = setting,
        swatches = listOf(g, c(goldDeep), o, c(rub), pt),
    )
}

internal object Palettes {
    /** Today's look, exact original values; only the faint grey and the soft red are lifted a little for contrast (owner pick 6a). */
    val GoldOxblood = Palette(
        "gold_oxblood", "Gold and Oxblood",
        gold = c(0xC9A55A), goldDeep = c(0x8C6D3A), goldDark = c(0x6B5430), goldLine = c(0x4A3A20), cap = c(0xA8884C),
        text = c(0xE6D9B8), textBright = c(0xF2E6C6), textSoft = c(0xD9CCAA), textDim = c(0xA89A76), textFaint = c(0x9A8760),
        oxblood = c(0x6A1D16), oxbloodDark = c(0x481510), rubric = c(0xA8352B), rubricSoft = c(0xD0645A),
        panelTop = c(0x1D1A14), panelBottom = c(0x12100C), ink = c(0x0A0907),
        shieldFill = c(0x15120D), shieldTop = c(0x3A1612), wax = c(0x4E1511), waxEdge = c(0x2A0A08),
        ember = c(0xF0A050), parchment = c(0xECDFBF), weatherGrey = c(0xA8987A),
        label = c(0xBFAE86), ribbonText = c(0xF0E2BD), goldLight = c(0xE3CF9C), summonDark = c(0x1C0A08),
        bgMid = c(0x26211A), bgEdge = c(0x0D0C09), glow = c(0xE5B25A), heart = c(0xFFA060), twinkle = c(0xFFF0C0),
        halo = Color(0x6BBE3C1E),
        coinA = c(0xE0C070), coinB = c(0x8A6A22), coinRim = c(0x5A4620), coinInk = c(0x3A2A0A),
        barA = c(0x5A2209), barB = c(0xC9772C),
        pennantA = c(0x7A5E2C), pennantB = c(0x5E4A24), pennantC = c(0x6E2A1E),
        capShadow = c(0x1C0605), capA = c(0xD9574A), capB = c(0xB03A2E), capC = c(0x6E1710), capD = c(0x9A2F25),
        shimmer = c(0xF3D27A), press = c(0xF0C86E), badge = c(0x6E1A14),
        feather = c(0x2A2216), gem = c(0x8A2A20), avatarA = c(0x2E2619), avatarB = c(0x14110B),
        hill1 = c(0x17140E), hill2 = c(0x0F0D09), leaf = c(0x1D1A14),
        sunFill = c(0xD4A04A), sunRays = c(0xD8B060), rain = c(0x8AA4C4), snow = c(0xECE6D6),
        bolt = c(0xE2C060), boltEdge = c(0xF4E4B0), windowGlow = c(0xE0963C), fog = c(0xA8A490),
        orbSmoke = R.drawable.medieval_orb_smoke, orbGlass = R.drawable.medieval_orb_glass, orbSetting = R.drawable.medieval_orb_setting,
        swatches = listOf(c(0xC9A55A), c(0x8C6D3A), c(0x6A1D16), c(0xA8352B), c(0x1D1A14)),
    )

    val Ember = scheme(
        "ember", "Ember",
        gold = 0xE3A04F, goldDeep = 0xA8692A, goldDark = 0x774A1E, goldLine = 0x4A2C14, cap = 0xC0843E,
        text = 0xEBD8B8, textBright = 0xF7E6C4, textSoft = 0xDDC7A0, textDim = 0xB09A78, textFaint = 0xA08760, label = 0xCDB488,
        ox = 0x7A1E14, oxDark = 0x4E120C, rub = 0xC0382A, rubSoft = 0xD86A58,
        panelTop = 0x1E1612, panelBottom = 0x120D0A, ink = 0x080605, bgMid = 0x2A1D14, bgEdge = 0x0C0907,
        shieldTop = 0x421812, wax = 0x5A1810, ember = 0xFFB060, parch = 0xF7E6C4, weather = 0xB49C78,
        glow = 0xF0A050, orbHeart = 0xFFB060, light = 0xF3D9A0,
        smoke = R.drawable.medieval_orb_smoke_ember, glass = R.drawable.medieval_orb_glass_ember, setting = R.drawable.medieval_orb_setting_ember,
    )

    val Verdigris = scheme(
        "verdigris", "Verdigris",
        gold = 0x6FB5A0, goldDeep = 0x3F7E6E, goldDark = 0x2D5A4E, goldLine = 0x1E3B33, cap = 0x6FA896,
        text = 0xD6E4D8, textBright = 0xEAF3E4, textSoft = 0xBCD0C2, textDim = 0x94AC9F, textFaint = 0x819B8F, label = 0xA5BFB2,
        ox = 0x1F5A4C, oxDark = 0x123A31, rub = 0xD0825A, rubSoft = 0xE0A07C,
        panelTop = 0x141A18, panelBottom = 0x0E1311, ink = 0x070A09, bgMid = 0x17211E, bgEdge = 0x090D0C,
        shieldTop = 0x123029, wax = 0x17463B, ember = 0x9FE0C8, parch = 0xE4EEDD, weather = 0x8FA89B,
        glow = 0x6FB5A0, orbHeart = 0x9FE0C8, light = 0xCFE8DA,
        smoke = R.drawable.medieval_orb_smoke_verdigris, glass = R.drawable.medieval_orb_glass_verdigris, setting = R.drawable.medieval_orb_setting_verdigris,
    )

    val Bone = scheme(
        "bone", "Bone",
        gold = 0xD8D2C4, goldDeep = 0x9A9486, goldDark = 0x6E695E, goldLine = 0x3C3A35, cap = 0xB0AA9A,
        text = 0xE8E4D8, textBright = 0xF6F3EA, textSoft = 0xD0CCBF, textDim = 0xA6A296, textFaint = 0x8E8A7E, label = 0xC0BBAC,
        ox = 0x6A1D16, oxDark = 0x481510, rub = 0xB23A30, rubSoft = 0xD2645A,
        panelTop = 0x1A1A19, panelBottom = 0x111110, ink = 0x080808, bgMid = 0x242422, bgEdge = 0x0C0C0B,
        shieldTop = 0x3A1612, wax = 0x4E1511, ember = 0xE8DCC0, parch = 0xF0EADA, weather = 0xA8A498,
        glow = 0xE8DCC0, orbHeart = 0xE8DCC0, light = 0xE8E2D4,
        smoke = R.drawable.medieval_orb_smoke_bone, glass = R.drawable.medieval_orb_glass_bone, setting = R.drawable.medieval_orb_setting_bone,
    )

    val RoyalSilver = scheme(
        "royal_silver", "Royal Blue and Silver",
        gold = 0xC4CCD8, goldDeep = 0x7F8AA0, goldDark = 0x55607A, goldLine = 0x2E3750, cap = 0x9AA6BE,
        text = 0xDCE2EE, textBright = 0xEEF2FA, textSoft = 0xC2CADA, textDim = 0x98A2B8, textFaint = 0x8793AB, label = 0xB3BDD0,
        ox = 0x22408C, oxDark = 0x142A64, rub = 0x5B86E8, rubSoft = 0x86A8F2,
        panelTop = 0x161A24, panelBottom = 0x0E1119, ink = 0x07090D, bgMid = 0x1B2130, bgEdge = 0x090B10,
        shieldTop = 0x142457, wax = 0x1B3A86, ember = 0xB4C8FF, parch = 0xE6ECF8, weather = 0x9AA6BC,
        glow = 0x7896FF, orbHeart = 0xB8CCFF, light = 0xDCE4F4,
        smoke = R.drawable.medieval_orb_smoke_royal, glass = R.drawable.medieval_orb_glass_royal, setting = R.drawable.medieval_orb_setting_royal,
    )

    val All = listOf(GoldOxblood, Ember, Verdigris, Bone, RoyalSilver)
    fun byId(id: String?): Palette = All.firstOrNull { it.id == id } ?: GoldOxblood
}

private const val PrefsName = "medieval_home"
private const val PaletteKey = "palette"

internal fun readPalette(context: Context): Palette =
    Palettes.byId(context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).getString(PaletteKey, null))

internal fun writePalette(context: Context, palette: Palette) {
    context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit().putString(PaletteKey, palette.id).apply()
}

private var paletteLoaded = false

/**
 * Reads the saved palette once per process. LauncherTheme calls it, so a launcher screen opened without Home
 * (settings brought back after the process was killed) shows the chosen colours and not the default.
 */
internal fun loadPaletteOnce(context: Context) {
    if (paletteLoaded) return
    paletteLoaded = true
    Medieval.palette = readPalette(context)
}
