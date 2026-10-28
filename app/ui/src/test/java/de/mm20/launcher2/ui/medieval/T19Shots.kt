package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * T19 live palette: Home is composed once in Gold, then the palette is switched inside the same composition
 * (no new activity, no rotation), like picking a scheme in Settings and pressing Back. The header (top part)
 * must look exactly like a Home that STARTED in that palette: a second Home is composed fresh (key bump) in the
 * same activity as the reference, and the two header pictures are compared.
 * Pictures go to docs/design/shots/T19/. Run: ./gradlew :app:ui:testDebugUnitTest --tests "*T19Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-420dpi")
class T19Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun goldToRoyal() = switch("royal", Palettes.RoyalSilver)
    @Test fun goldToEmber() = switch("ember", Palettes.Ember)
    @Test fun goldToBone() = switch("bone", Palettes.Bone)
    @Test fun goldToVerdigris() = switch("verdigris", Palettes.Verdigris)

    private fun switch(name: String, to: Palette) {
        writePalette(rule.activity, Palettes.GoldOxblood)
        val owner = MedievalShots.FakeOwner()
        var fresh by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(fresh) {
                    MedievalHomeComponent.Component(Modifier.fillMaxSize(), PaddingValues(), mockk<LauncherScaffoldState>(relaxed = true))
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        save("gold-before-$name", shot())

        // same composition, no activity recreate
        rule.runOnUiThread { Medieval.palette = to }
        rule.mainClock.advanceTimeBy(1500)
        val switched = shot()
        save("gold-to-$name", switched)

        // reference: a Home that starts in the new palette
        writePalette(rule.activity, to)
        rule.runOnUiThread { fresh++ }
        rule.mainClock.advanceTimeBy(1500)
        val reference = shot()
        save("started-in-$name", reference)

        val head = (switched.height * 0.28f).toInt()
        val bad = differing(switched, reference, head)
        val share = bad * 1000 / (head * switched.width)
        println("T19 $name: header pixels that differ from a Home started in $name: $bad (${share / 10.0}%)")
        assertTrue("header differs from a fresh $name Home: $bad pixels", share < 5)
    }

    private fun differing(a: Bitmap, b: Bitmap, h: Int): Int {
        var n = 0
        for (y in 0 until h) for (x in 0 until a.width) {
            val p = a.getPixel(x, y); val q = b.getPixel(x, y)
            val d = Math.abs(((p shr 16) and 255) - ((q shr 16) and 255)) +
                Math.abs(((p shr 8) and 255) - ((q shr 8) and 255)) + Math.abs((p and 255) - (q and 255))
            if (d > 40) n++
        }
        return n
    }

    private fun shot() = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/T19").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
