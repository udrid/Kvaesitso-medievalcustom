package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import android.graphics.Color as AColor
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import de.mm20.launcher2.ui.locals.LocalBackStack
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
 * F3 final check of T19: the WHOLE Home (not only the header), the open typing layer, and a Settings screen with a
 * dialog are composed once in Gold; the palette is switched inside the same composition; the picture is compared
 * with a screen composed fresh in the new palette, band by band (10 horizontal bands of the picture).
 * Pictures and a red "what differs" map go to docs/design/shots/F3/.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*F3PaletteLive*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-420dpi")
class F3PaletteLive {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test fun homeToRoyal() = home("royal", Palettes.RoyalSilver, false)
    @Test fun homeToEmber() = home("ember", Palettes.Ember, false)
    @Test fun homeToBone() = home("bone", Palettes.Bone, false)
    @Test fun homeToVerdigris() = home("verdigris", Palettes.Verdigris, false)
    @Test fun typingToRoyal() = home("royal", Palettes.RoyalSilver, true)
    @Test fun typingToEmber() = home("ember", Palettes.Ember, true)
    @Test fun typingToBone() = home("bone", Palettes.Bone, true)
    @Test fun typingToVerdigris() = home("verdigris", Palettes.Verdigris, true)
    @Test fun settingsToRoyal() = settings("royal", Palettes.RoyalSilver)
    @Test fun settingsToEmber() = settings("ember", Palettes.Ember)
    @Test fun settingsToBone() = settings("bone", Palettes.Bone)
    @Test fun settingsToVerdigris() = settings("verdigris", Palettes.Verdigris)

    /** T20: the saved choice is read fresh from the phone's storage (as after the process was killed). */
    @Test fun cornerStyleSurvivesProcessDeath() {
        val ctx = rule.activity.applicationContext
        val prefs = ctx.getSharedPreferences("medieval_home", 0)
        prefs.edit().remove("corner_style").commit()
        org.junit.Assert.assertEquals(CornerStyle.Sharp, readCornerStyle(ctx))
        writeCornerStyle(ctx, CornerStyle.Round)
        Corners.style = CornerStyle.Sharp // a new process starts with the default until the theme loads it
        org.junit.Assert.assertEquals(CornerStyle.Round, readCornerStyle(ctx))
        prefs.edit().putString("corner_style", "garbage").commit()
        org.junit.Assert.assertEquals(CornerStyle.Sharp, readCornerStyle(ctx))
    }

    private fun home(name: String, to: Palette, typing: Boolean) {
        val tag = (if (typing) "typing" else "home") + "-" + name
        writePalette(rule.activity, Palettes.GoldOxblood)
        writeCornerStyle(rule.activity, CornerStyle.Sharp)
        Corners.style = CornerStyle.Sharp
        Medieval.palette = Palettes.GoldOxblood
        val owner = MedievalShots.FakeOwner()
        var fresh by mutableIntStateOf(0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                Themed {
                    key(fresh) {
                        MedievalHomeComponent.Component(Modifier.fillMaxSize(), PaddingValues(), mockk<LauncherScaffoldState>(relaxed = true))
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        if (typing) {
            rule.onNodeWithText("Command the Oracle", substring = true).performClick()
            rule.mainClock.advanceTimeBy(1500)
        }
        save("$tag-1-gold", shot())
        rule.runOnUiThread { Medieval.palette = to; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        rule.mainClock.advanceTimeBy(1500)
        val switched = shot()
        save("$tag-2-switched", switched)
        // reference: same screen state, started in the new palette (Home re-reads the saved palette on start)
        writePalette(rule.activity, to)
        rule.runOnUiThread { fresh++ }
        rule.mainClock.advanceTimeBy(1500)
        if (typing) {
            rule.onNodeWithText("Command the Oracle", substring = true).performClick()
            rule.mainClock.advanceTimeBy(1500)
        }
        val reference = shot()
        save("$tag-3-fresh", reference)
        compare(tag, switched, reference)
    }

    private fun settings(name: String, to: Palette) {
        val tag = "settings-$name"
        Medieval.palette = Palettes.GoldOxblood
        Corners.style = CornerStyle.Sharp
        rule.mainClock.autoAdvance = false
        var fresh by mutableIntStateOf(0)
        rule.setContent {
            Themed {
                key(fresh) {
                    Box(Modifier.fillMaxSize()) {
                        CompositionLocalProvider(LocalBackStack provides mockk(relaxed = true)) {
                            PreferenceScreen(title = { Text("Appearance") }) {
                                item {
                                    PreferenceCategory {
                                        Preference(title = "Transparency", summary = "Default", onClick = {})
                                        SwitchPreference(title = "Animations", summary = "On", value = true, onValueChanged = {})
                                        SwitchPreference(title = "Sound", summary = "Off", value = false, onValueChanged = {})
                                        MedievalCornerPreference()
                                    }
                                }
                            }
                        }
                        Box(Modifier.fillMaxSize().padding(top = 520.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.TopCenter) {
                            DialogPanel(
                                title = { Text("Hide this item?") },
                                text = { Text("It will no longer show up in search results.") },
                                dismissButton = { OutlinedButton(onClick = {}) { Text("Cancel") } },
                                confirmButton = { Button(onClick = {}) { Text("Hide") } },
                            )
                        }
                    }
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        save("$tag-1-gold", shot())
        rule.runOnUiThread { Medieval.palette = to; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        rule.mainClock.advanceTimeBy(1500)
        val switched = shot()
        save("$tag-2-switched", switched)
        rule.runOnUiThread { fresh++ }
        rule.mainClock.advanceTimeBy(1500)
        val reference = shot()
        save("$tag-3-fresh", reference)
        compare(tag, switched, reference)
    }

    @Composable
    private fun Themed(content: @Composable () -> Unit) {
        // like LauncherTheme: the Material scheme and the corner style read the live state
        MaterialTheme(colorScheme = Deliverance.colorScheme, shapes = Deliverance.shapes, typography = Deliverance.typography) {
            CompositionLocalProvider(LocalCornerStyle provides Corners.style) {
                Box(Modifier.fillMaxSize().background(Medieval.Ink)) { content() }
            }
        }
    }

    private fun compare(tag: String, a: Bitmap, b: Bitmap) {
        val bands = 10
        val bandH = a.height / bands
        val map = a.copy(Bitmap.Config.ARGB_8888, true)
        val worst = StringBuilder()
        var total = 0
        var maxShare = 0
        for (band in 0 until bands) {
            var n = 0
            for (y in band * bandH until (band + 1) * bandH) for (x in 0 until a.width) {
                val p = a.getPixel(x, y); val q = b.getPixel(x, y)
                val d = Math.abs(AColor.red(p) - AColor.red(q)) + Math.abs(AColor.green(p) - AColor.green(q)) + Math.abs(AColor.blue(p) - AColor.blue(q))
                if (d > 40) { n++; map.setPixel(x, y, AColor.RED) }
            }
            val share = n * 1000 / (bandH * a.width)
            total += n
            if (share > maxShare) maxShare = share
            worst.append(" b$band=${share / 10.0}%")
        }
        save("$tag-4-diffmap", map)
        println("F3 $tag differing share per band:$worst  total=${total * 1000 / (a.width * bandH * bands) / 10.0}%")
        assertTrue("$tag: a band of the switched picture differs from a fresh start ($worst)", maxShare < 30)
    }

    private fun shot() = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/F3").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
