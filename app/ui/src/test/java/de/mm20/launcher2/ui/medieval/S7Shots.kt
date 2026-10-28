package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.center
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.down
import androidx.compose.ui.test.up
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * S7 welcome: Home is composed with a hand-driven Welcome (LocalWelcomeOverride) and the clock is set to the key
 * frames, in Gold and in Ember. A touch in the middle must end it (ms back to -1, the normal Home is drawn), and
 * the finished frame must equal "not playing". Pictures go to docs/design/shots/S7/.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*S7Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-420dpi")
class S7Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun gold() = frames("gold", Palettes.GoldOxblood)
    @Test fun ember() = frames("ember", Palettes.Ember)
    @Test fun tapMidway() = tap()

    private fun start(p: Palette): Welcome {
        writePalette(rule.activity, p)
        Medieval.palette = p
        val owner = MedievalShots.FakeOwner()
        val welcome = Welcome()
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalWelcomeOverride provides welcome) {
                MedievalHomeComponent.Component(Modifier.fillMaxSize(), PaddingValues(), mockk<LauncherScaffoldState>(relaxed = true))
            }
        }
        rule.mainClock.advanceTimeBy(1200)
        return welcome
    }

    private fun frames(name: String, p: Palette) {
        val w = start(p)
        for (ms in listOf(0L, 300L, 600L, 900L, 1200L, 1500L)) {
            rule.runOnUiThread { w.ms = ms }
            rule.mainClock.advanceTimeBy(100)
            save("$name-$ms", shot())
        }
        rule.runOnUiThread { w.stop() }
        rule.mainClock.advanceTimeBy(100)
        save("$name-normal", shot())
    }

    private fun tap() {
        val w = start(Palettes.GoldOxblood)
        rule.runOnUiThread { w.ms = 600L }
        rule.mainClock.advanceTimeBy(100)
        save("tap-before", shot())
        assertTrue(w.playing)
        rule.onRoot().performTouchInput { down(center); up() }
        rule.mainClock.advanceTimeBy(100)
        assertFalse("a touch must end the welcome", w.playing)
        assertEquals(1f, w.alpha(Rise.Dispatches), 0f)
        save("tap-after", shot())
    }

    private fun shot() = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/S7").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
