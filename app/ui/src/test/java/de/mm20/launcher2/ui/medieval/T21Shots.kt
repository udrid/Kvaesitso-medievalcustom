package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.locals.LocalBackStack
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * T21 Always-on display pictures: the settings row and the page, in Gold and Ember, Sharp and Round.
 * Pictures go to docs/design/shots/T22/. Run: ./gradlew :app:ui:testDebugUnitTest --tests "*T21Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h1400dp-port-420dpi")
class T21Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun gold() = shoot("gold", Palettes.GoldOxblood, CornerStyle.Sharp)
    @Test fun ember() = shoot("ember", Palettes.Ember, CornerStyle.Sharp)
    @Test fun goldRound() = shoot("gold-round", Palettes.GoldOxblood, CornerStyle.Round)

    private fun shoot(tag: String, palette: Palette, corner: CornerStyle) {
        Medieval.palette = palette
        Corners.style = corner
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Themed {
                PreferenceScreen(title = "Settings") {
                    item {
                        PreferenceCategory {
                            MedievalAodPreference {}
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        save("row-$tag", shot())
    }

    @Test fun pageGold() = page("gold", Palettes.GoldOxblood, null)
    @Test fun pageEmber() = page("ember", Palettes.Ember, "Saved to Pictures/Deliverance")

    private fun page(tag: String, palette: Palette, status: String?) {
        Medieval.palette = palette
        Corners.style = CornerStyle.Sharp
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Themed { AodSettingsContent("mars", status, {}, {}, {}, lockStatus = if (tag == "ember") "Done. Lock the phone to see it." else null, canRestore = tag == "ember") }
        }
        rule.mainClock.advanceTimeBy(1500)
        save("t22-page-$tag", shot())
    }

    @Composable
    private fun Themed(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = Deliverance.colorScheme,
            shapes = Deliverance.shapes,
            typography = Deliverance.typography,
        ) {
            CompositionLocalProvider(LocalCornerStyle provides Corners.style, LocalBackStack provides mockk(relaxed = true)) {
                Box(Modifier.fillMaxSize().background(Medieval.Ink)) {
                    GrainAndVignette(Modifier.fillMaxSize(), rim = 0.3f)
                    content()
                }
            }
        }
    }

    private fun shot() = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/T22").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
