package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import android.content.pm.ApplicationInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * S5 apps hub pictures (swipe-up page, top part): collapsed, one category open, filter, long-press menu,
 * Gold and Ember, portrait and landscape. Run: ./gradlew :app:ui:testDebugUnitTest --tests "*S5Shots*"
 * Pictures go to docs/design/shots/S5/. The fake apps below are example names, not the owner's phone.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-420dpi")
class S5Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val apps = listOf(
        "com.google.android.apps.messaging" to "Messages", "com.samsung.android.dialer" to "Phone",
        "com.google.android.gm" to "Gmail", "com.discord" to "Discord", "org.thoughtcrime.securesms" to "Signal",
        "com.twitter.android" to "X", "com.instagram.android" to "Instagram", "com.reddit.frontpage" to "Reddit",
        "com.spotify.music" to "Spotify", "com.google.android.youtube" to "YouTube", "com.sec.android.app.camera" to "Camera",
        "com.sec.android.app.popupcalculator" to "Calculator", "com.sec.android.app.clockpackage" to "Clock",
        "com.sec.android.app.myfiles" to "My Files", "com.samsung.android.app.notes" to "Samsung Notes",
        "com.android.chrome" to "Chrome", "com.sec.android.app.voicenote" to "Voice Recorder",
        "com.amazon.mShop.android.shopping" to "Amazon", "com.paypal.android.p2pmobile" to "PayPal",
        "com.google.android.apps.maps" to "Maps", "com.waze" to "Waze", "com.google.android.calendar" to "Calendar",
        "com.google.android.apps.docs" to "Drive", "com.android.settings" to "Settings",
        "com.android.vending" to "Play Store", "com.supercell.clashofclans" to "Clash of Clans",
        "com.example.unknown.one" to "Zither", "com.example.unknown.two" to "Quill",
    ).map { (p, l) -> HubApp(p, l, ApplicationInfo.CATEGORY_UNDEFINED, p.startsWith("com.android.")) }

    private val today = HubToday("10:30", "Example event", needsCalendar = false, weather = "14° Fair")

    @Before fun setUp() {
        Medieval.palette = Palettes.GoldOxblood
        getSharedPrefs().edit().clear().apply()
    }

    @After fun tearDown() { Medieval.palette = Palettes.GoldOxblood }

    private fun getSharedPrefs() = rule.activity.getSharedPreferences("medieval_home", android.content.Context.MODE_PRIVATE)

    @Composable
    private fun Stage() {
        androidx.compose.material3.MaterialTheme(colorScheme = Deliverance.colorScheme, shapes = Deliverance.shapes, typography = Deliverance.typography) {
        Box(Modifier.fillMaxSize().background(Medieval.Ink)) {
            GrainAndVignette(Modifier.fillMaxSize(), rim = 0.3f)
            Column(Modifier.verticalScroll(rememberScrollState())) {
                AppsHub(apps, today, tick = 0)
            }
        }
        }
    }

    @Test fun favourites() {
        HubPrefs.setStar(rule.activity, "com.sec.android.app.camera", true)
        HubPrefs.setStar(rule.activity, "com.discord", true)
        HubPrefs.setStar(rule.activity, "com.spotify.music", true)
        rule.setContent { Stage() }
        save("favourites-gold-portrait")
    }

    @Test fun collapsed() { rule.setContent { Stage() }; save("collapsed-gold-portrait") }

    @Test fun expanded() {
        rule.setContent { Stage() }
        rule.onNodeWithText("TOOLS", substring = true).performClick()
        save("expanded-gold-portrait")
    }

    @Test fun filter() {
        rule.setContent { Stage() }
        // the search field is the only editable node
        rule.onNodeWithText("Search your apps...").performClick()
        rule.onNode(hasSetTextAction()).performTextInput("ca")
        save("filter-gold-portrait")
    }

    @Test fun menu() {
        rule.setContent { Stage() }
        rule.onNodeWithText("TOOLS", substring = true).performClick()
        rule.onNodeWithText("CALCULATOR", substring = true).performTouchInput { longClick() }
        save("menu-gold-portrait")
        rule.onNodeWithText("MOVE TO CATEGORY...", ignoreCase = true).performClick()
        save("menu-move-gold-portrait")
    }

    @Test fun moveIsKept() {
        // the menu item itself is checked in the pictures; here the stored move must survive a fresh load
        HubPrefs.move(rule.activity, "com.example.unknown.one", AppCategory.Work)
        rule.setContent { Stage() }
        rule.onNodeWithText("WORK", substring = true).performClick()
        rule.onNodeWithText("ZITHER", substring = true).assertIsDisplayed()
        assertEquals(AppCategory.Work, HubPrefs.moved(rule.activity)["com.example.unknown.one"])
        assertTrue(AppCategories.categoryOf(apps.first { it.label == "Zither" }, HubPrefs.moved(rule.activity)) == AppCategory.Work)
    }

    @Test fun rules() {
        val moved = emptyMap<String, AppCategory>()
        fun cat(p: String, c: Int = -1, sys: Boolean = false) = AppCategories.categoryOf(HubApp(p, "x", c, sys), moved)
        assertEquals(AppCategory.Communication, cat("com.discord"))
        assertEquals(AppCategory.Games, cat("com.supercell.brawlstars"))
        assertEquals(AppCategory.Media, cat("com.unlisted.player", ApplicationInfo.CATEGORY_VIDEO))
        assertEquals(AppCategory.System, cat("com.samsung.unlisted", sys = true))
        assertEquals(AppCategory.Other, cat("com.unlisted.app"))
        assertEquals(AppCategory.Tools, AppCategories.categoryOf(HubApp("com.discord", "x"), mapOf("com.discord" to AppCategory.Tools)))
    }

    @Test fun emberPortrait() {
        Medieval.palette = Palettes.Ember
        rule.setContent { Stage() }
        rule.onNodeWithText("TOOLS", substring = true).performClick()
        save("expanded-ember-portrait")
    }

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landscapeGold() {
        rule.setContent { Stage() }
        rule.onNodeWithText("TOOLS", substring = true).performClick()
        save("expanded-gold-landscape")
    }

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landscapeEmber() {
        Medieval.palette = Palettes.Ember
        rule.setContent { Stage() }
        save("collapsed-ember-landscape")
    }

    private fun save(name: String) {
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/S5").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
