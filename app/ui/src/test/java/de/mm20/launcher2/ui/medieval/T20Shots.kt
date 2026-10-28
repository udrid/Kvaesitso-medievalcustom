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
import androidx.compose.material3.MaterialTheme
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
 * T20 Corner style: a dialog's buttons and the typing-layer chips in Sharp and Round, changed while the screen
 * stays open (live), in three palettes, plus Settings > Appearance. Pictures go to docs/design/shots/T20/.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*T20Shots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class T20Shots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val navPx = 126
    private val cutPx = 84

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun dialogGold() = dialog("gold", Palettes.GoldOxblood)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun dialogEmber() = dialog("ember", Palettes.Ember)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun dialogRoyal() = dialog("royal", Palettes.RoyalSilver)

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun typingGold() = typing("gold", Palettes.GoldOxblood)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun typingEmber() = typing("ember", Palettes.Ember)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun typingRoyal() = typing("royal", Palettes.RoyalSilver)

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi") fun appearance() {
        Medieval.palette = Palettes.GoldOxblood
        Corners.style = CornerStyle.Sharp
        rule.mainClock.autoAdvance = false
        rule.setContent { Themed { Appearance() } }
        rule.mainClock.advanceTimeBy(1500)
        save("appearance-closed", shot())
        rule.onNodeWithText("Corner style").performClick()
        rule.mainClock.advanceTimeBy(1500)
        save("appearance-open", shot())
    }

    /** The panel stays; the button corners change when the setting changes, without a restart. */
    private fun dialog(tag: String, palette: Palette) {
        Medieval.palette = palette
        Corners.style = CornerStyle.Sharp
        rule.mainClock.autoAdvance = false
        rule.setContent {
            Themed {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    DialogPanel(
                        title = { Text("Hide this item?") },
                        text = { Text("It will no longer show up in search results. You can show it again in the settings.") },
                        dismissButton = { OutlinedButton(onClick = {}) { Text("Cancel") } },
                        confirmButton = { Button(onClick = {}) { Text("Hide") } },
                    )
                }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        save("dialog-$tag-sharp", shot())
        rule.runOnUiThread { Corners.style = CornerStyle.Round; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        rule.mainClock.advanceTimeBy(1500)
        save("dialog-$tag-round", shot())
    }

    private fun typing(tag: String, palette: Palette) {
        writePalette(rule.activity, palette)
        writeCornerStyle(rule.activity, CornerStyle.Sharp)
        Corners.style = CornerStyle.Sharp
        val owner = MedievalShots.FakeOwner()
        rule.mainClock.autoAdvance = false
        val activity = rule.activity
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                Themed {
                    MedievalHomeComponent.Component(
                        Modifier.fillMaxSize(),
                        PaddingValues(),
                        mockk<LauncherScaffoldState>(relaxed = true),
                    )
                }
            }
        }
        val root = activity.window.decorView
        val w = root.width
        val h = root.height
        val nav = Insets.of(0, 0, 0, navPx)
        val hole = Rect(w / 2 - 45, 0, w / 2 + 45, cutPx)
        val safe = Rect(0, cutPx, 0, 0)
        val status = Insets.of(0, cutPx, 0, 0)
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.navigationBars(), nav)
            .setInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars(), nav)
            .setInsets(WindowInsetsCompat.Type.statusBars(), status)
            .setInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars(), status)
            .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(safe))
            .setDisplayCutout(DisplayCutoutCompat(safe, listOf(hole)))
            .build()
        rule.runOnUiThread { ViewCompat.dispatchApplyWindowInsets(root, insets) }
        rule.mainClock.advanceTimeBy(1500)
        rule.onNodeWithText("Command the Oracle", substring = true).performClick()
        rule.mainClock.advanceTimeBy(1500)
        save("typing-$tag-sharp", shot())
        rule.runOnUiThread { Corners.style = CornerStyle.Round; androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications() }
        rule.mainClock.advanceTimeBy(1500)
        save("typing-$tag-round", shot())
    }

    @Composable
    private fun Themed(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = Deliverance.colorScheme,
            shapes = Deliverance.shapes,
            typography = Deliverance.typography,
        ) {
            CompositionLocalProvider(LocalCornerStyle provides Corners.style) {
                Box(Modifier.fillMaxSize().background(Medieval.Ink)) {
                    GrainAndVignette(Modifier.fillMaxSize(), rim = 0.3f)
                    content()
                }
            }
        }
    }

    @Composable
    private fun Appearance() {
        CompositionLocalProvider(LocalBackStack provides mockk(relaxed = true)) {
            PreferenceScreen(title = { Text("Appearance") }) {
                item {
                    PreferenceCategory {
                        Preference(title = "Transparency", summary = "Default", onClick = {})
                        MedievalCornerPreference()
                    }
                }
            }
        }
    }

    private fun shot() = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)

    private fun save(name: String, bmp: Bitmap) {
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/T20").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
