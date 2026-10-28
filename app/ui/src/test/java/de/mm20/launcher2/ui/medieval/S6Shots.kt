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
 * S6 landscape fill (option D): Home in landscape with the taller columns and the Forecast strip, both turns,
 * two palettes, small screen, large font, quiet (empty) and busy (full) states, plus portrait for the baseline.
 * Pictures go to docs/design/shots/S6/. Run: ./gradlew :app:ui:testDebugUnitTest --tests "*S6Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class S6Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val navPx = 126
    private val cutPx = 84
    private val gold = Palettes.GoldOxblood
    private val ember = Palettes.Ember

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landLeftFullGold() = home("land-left-full-gold", gold, Side.Right, Side.Left, full = true)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landRightFullEmber() = home("land-right-full-ember", ember, Side.Left, Side.Right, full = true)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landLeftEmptyGold() = home("land-left-empty-gold", gold, Side.Right, Side.Left, full = false)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi") fun landRightEmptyEmber() = home("land-right-empty-ember", ember, Side.Left, Side.Right, full = false)
    @Test @Config(qualifiers = "w640dp-h360dp-land-xhdpi") fun smallLandFullGold() = home("land-small-640x360-full-gold", gold, Side.Right, Side.Left, full = true)

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun largeFontLand() {
        RuntimeEnvironment.setFontScale(1.3f)
        home("land-left-font130-full-gold", gold, Side.Right, Side.Left, full = true)
    }

    // baseline: portrait must look as before
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun portraitFullGold() = home("portrait-full-gold", gold, Side.Bottom, Side.Top, full = true)
    @Test @Config(qualifiers = "w360dp-h640dp-port-xhdpi") fun smallPortraitEmber() = home("portrait-small-360x640-ember", ember, Side.Bottom, Side.Top, full = false)

    private enum class Side { Left, Top, Right, Bottom }

    private fun home(name: String, palette: Palette, nav: Side, camera: Side, full: Boolean = false, openCommand: Boolean = false) {
        fakeHomeFull = full
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
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/S6").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
