package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.ui.component.LauncherCard
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SliderPreference
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import de.mm20.launcher2.ui.launcher.search.common.list.ListResults
import de.mm20.launcher2.ui.locals.LocalBackStack
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * F1 final check of the combined build (R1 + S2 + S4 + S3): Home and the S2 screens in several palettes,
 * both landscape turns, a small screen and a large font. Pictures go to docs/design/shots/F1/.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*F1Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class F1Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val navPx = 126
    private val cutPx = 84

    private val gold = Palettes.GoldOxblood
    private val ember = Palettes.Ember
    private val bone = Palettes.Bone
    private val royal = Palettes.RoyalSilver
    private val verdigris = Palettes.Verdigris

    // ---- Home, portrait, every palette
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun homePortraitGold() = home("home-portrait-gold", gold, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun homePortraitEmber() = home("home-portrait-ember", ember, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun homePortraitBone() = home("home-portrait-bone", bone, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun homePortraitRoyal() = home("home-portrait-royal", royal, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun homePortraitVerdigris() = home("home-portrait-verdigris", verdigris, Side.Bottom, Side.Top)

    // ---- Home, both landscape turns
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landLeftGold() = home("home-land-camera-left-gold", gold, Side.Right, Side.Left)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landRightGold() = home("home-land-camera-right-gold", gold, Side.Left, Side.Right)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landLeftEmber() = home("home-land-camera-left-ember", ember, Side.Right, Side.Left)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landRightBone() = home("home-land-camera-right-bone", bone, Side.Left, Side.Right)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landLeftRoyal() = home("home-land-camera-left-royal", royal, Side.Right, Side.Left)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landTypingEmber() = home("home-land-typing-ember", ember, Side.Right, Side.Left, openCommand = true)

    // ---- Home, small screen and large font
    @Test @Config(qualifiers = "w360dp-h640dp-port-xhdpi") fun smallPortraitGold() = home("home-small-360x640-gold", gold, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w360dp-h640dp-port-xhdpi") fun smallPortraitBone() = home("home-small-360x640-bone", bone, Side.Bottom, Side.Top)
    @Test @Config(qualifiers = "w640dp-h360dp-land-xhdpi") fun smallLandEmber() = home("home-small-640x360-ember", ember, Side.Right, Side.Left)

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun largeFontPortrait() {
        RuntimeEnvironment.setFontScale(1.3f)
        home("home-portrait-font130-gold", gold, Side.Bottom, Side.Top)
    }

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun largeFontLand() {
        RuntimeEnvironment.setFontScale(1.3f)
        home("home-land-camera-left-font130-bone", bone, Side.Right, Side.Left)
    }

    // ---- S2 screens in the palettes
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun settingsGold() = screen("s2-settings-gold", gold) { Settings() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun settingsEmber() = screen("s2-settings-ember", ember) { Settings() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun settingsBone() = screen("s2-settings-bone", bone) { Settings() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun settingsRoyal() = screen("s2-settings-royal", royal) { Settings() }
    @Test @Config(qualifiers = "w360dp-h640dp-port-xhdpi") fun settingsSmallVerdigris() = screen("s2-settings-360x640-verdigris", verdigris) { Settings() }

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun settingsLargeFont() {
        RuntimeEnvironment.setFontScale(1.3f)
        screen("s2-settings-font130-gold", gold) { Settings() }
    }

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun settingsLandEmber() = screen("s2-settings-land-ember", ember) { Settings() }

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun resultsGold() = screen("s2-results-gold", gold) { Results() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun resultsBone() = screen("s2-results-bone", bone) { Results() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun resultsRoyal() = screen("s2-results-royal", royal) { Results() }
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun resultsLandEmber() = screen("s2-results-land-ember", ember) { Results() }

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun dialogEmber() = screen("s2-dialog-ember", ember) { Dialog() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun dialogRoyal() = screen("s2-dialog-royal", royal) { Dialog() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun sheetCardsBone() = screen("s2-sheet-cards-bone", bone) { SheetAndCards() }
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun sheetCardsGold() = screen("s2-sheet-cards-gold", gold) { SheetAndCards() }

    // ---- S2 contents

    private fun fake(name: String): SavableSearchable = mockk<SavableSearchable>(relaxed = true).also {
        every { it.key } returns name
        every { it.label } returns name
    }

    @Composable
    private fun Settings() {
        CompositionLocalProvider(LocalBackStack provides mockk(relaxed = true)) {
            PreferenceScreen(title = { Text("Settings") }) {
                item {
                    PreferenceCategory {
                        MedievalPalettePreference()
                        MedievalAnimationsPreference()
                    }
                }
                item {
                    PreferenceCategory("Search") {
                        SwitchPreference(title = "Contacts", value = true, onValueChanged = {})
                        SwitchPreference(title = "Show labels", value = false, onValueChanged = {})
                        SliderPreference(title = "Grid columns", value = 5f, min = 3f, max = 8f, step = 1f, onValueChanged = {})
                    }
                }
                item {
                    PreferenceCategory("Information") {
                        Preference(title = "About", summary = "Deliverance, a fork of Kvaesitso", onClick = {})
                    }
                }
            }
        }
    }

    @Composable
    private fun Results() {
        LazyColumn(contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp)) {
            ListResults(
                title = "Apps", key = "apps",
                items = listOf(fake("Maps"), fake("Marketplace"), fake("Messages")),
                itemContent = { item, _, _ -> Text(item.label, Modifier.padding(16.dp)) },
            )
            ListResults(
                title = "Contacts", key = "contacts",
                items = listOf(fake("Marc Becker")),
                itemContent = { item, _, _ -> Text(item.label, Modifier.padding(16.dp)) },
            )
        }
    }

    @Composable
    private fun Dialog() {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            DialogPanel(
                title = { Text("Hide this item?") },
                text = { Text("It will no longer show up in search results. You can show it again in the settings.") },
                dismissButton = { OutlinedButton(onClick = {}) { Text("Cancel") } },
                confirmButton = { Button(onClick = {}) { Text("Hide") } },
            )
        }
    }

    @Composable
    private fun SheetAndCards() {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp)) {
                LauncherCard(Modifier.padding(bottom = 12.dp)) { Text("Today: 10:24", Modifier.padding(16.dp)) }
                LauncherCard(Modifier.padding(bottom = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Calendar: Market day, 10:00")
                        Switch(checked = true, onCheckedChange = {})
                        Switch(checked = false, onCheckedChange = {})
                    }
                }
            }
            Box(Modifier.align(Alignment.BottomCenter)) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    SheetBody { Text("Restore backup", Modifier.padding(16.dp)) }
                }
            }
        }
    }

    // ---- drawing

    private enum class Side { Left, Top, Right, Bottom }

    /** S2 screens inside the same theme LauncherTheme hands out (colours follow the palette). */
    private fun screen(name: String, palette: Palette, content: @Composable () -> Unit) {
        Medieval.palette = palette
        rule.mainClock.autoAdvance = false
        rule.setContent {
            MaterialTheme(
                colorScheme = Deliverance.colorScheme,
                shapes = Deliverance.shapes,
                typography = Deliverance.typography,
            ) {
                Box(Modifier.fillMaxSize().background(Medieval.Ink)) {
                    GrainAndVignette(Modifier.fillMaxSize(), rim = 0.3f)
                    content()
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        save(name, rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true))
    }

    private fun home(name: String, palette: Palette, nav: Side, camera: Side, openCommand: Boolean = false) {
        // Home reads the saved palette when it starts, like on the phone
        writePalette(rule.activity, palette)
        val owner = MedievalShots.FakeOwner()
        rule.mainClock.autoAdvance = false
        val activity = rule.activity
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                MedievalHomeComponent.Component(
                    Modifier.fillMaxSize(),
                    PaddingValues(),
                    mockk<LauncherScaffoldState>(relaxed = true),
                )
            }
        }
        val root = activity.window.decorView
        val w = root.width
        val h = root.height
        val navInsets = when (nav) {
            Side.Left -> Insets.of(navPx, 0, 0, 0)
            Side.Right -> Insets.of(0, 0, navPx, 0)
            Side.Bottom -> Insets.of(0, 0, 0, navPx)
            Side.Top -> Insets.NONE
        }
        val hole = when (camera) {
            Side.Left -> Rect(0, h / 2 - 45, cutPx, h / 2 + 45)
            Side.Right -> Rect(w - cutPx, h / 2 - 45, w, h / 2 + 45)
            Side.Top -> Rect(w / 2 - 45, 0, w / 2 + 45, cutPx)
            Side.Bottom -> Rect(w / 2 - 45, h - cutPx, w / 2 + 45, h)
        }
        val safe = when (camera) {
            Side.Left -> Rect(cutPx, 0, 0, 0)
            Side.Right -> Rect(0, 0, cutPx, 0)
            Side.Top -> Rect(0, cutPx, 0, 0)
            Side.Bottom -> Rect(0, 0, 0, cutPx)
        }
        val status = if (camera == Side.Top) Insets.of(0, cutPx, 0, 0) else Insets.NONE
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.navigationBars(), navInsets)
            .setInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars(), navInsets)
            .setInsets(WindowInsetsCompat.Type.statusBars(), status)
            .setInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars(), status)
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(safe))
            .setDisplayCutout(DisplayCutoutCompat(safe, listOf(hole)))
            .build()
        rule.runOnUiThread { ViewCompat.dispatchApplyWindowInsets(root, insets) }
        rule.mainClock.advanceTimeBy(1500)
        if (openCommand) {
            rule.onNodeWithText("Command the Oracle", substring = true).performClick()
            rule.mainClock.advanceTimeBy(1500)
        }
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        // navigation bar (blue) and camera hole (red), as in MedievalShots
        val c = Canvas(bmp)
        val p = Paint().apply { color = 0x553060FF }
        when (nav) {
            Side.Left -> c.drawRect(0f, 0f, navPx.toFloat(), h.toFloat(), p)
            Side.Right -> c.drawRect((w - navPx).toFloat(), 0f, w.toFloat(), h.toFloat(), p)
            Side.Bottom -> c.drawRect(0f, (h - navPx).toFloat(), w.toFloat(), h.toFloat(), p)
            Side.Top -> Unit
        }
        p.color = 0xAAFF2020.toInt()
        c.drawCircle(hole.exactCenterX(), hole.exactCenterY(), 22f, p)
        save(name, bmp)
    }

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/F1").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
