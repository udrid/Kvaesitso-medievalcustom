package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import de.mm20.launcher2.search.SavableSearchable
import de.mm20.launcher2.ui.component.LauncherCard
import de.mm20.launcher2.ui.component.preferences.Preference
import de.mm20.launcher2.ui.component.preferences.PreferenceCategory
import de.mm20.launcher2.ui.component.preferences.PreferenceScreen
import de.mm20.launcher2.ui.component.preferences.SliderPreference
import de.mm20.launcher2.ui.component.preferences.SwitchPreference
import de.mm20.launcher2.ui.launcher.search.common.list.ListResults
import de.mm20.launcher2.ui.locals.LocalBackStack
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * S2-screens screenshot tests: the restyled launcher screens rendered on the PC with fake data
 * (ops/emulator.md). Pictures go to docs/design/shots/s2-*.png. Look at them; this is not pass/fail.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*DeliveranceShots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h914dp-port-420dpi")
class DeliveranceShots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Composable
    private fun Stage(content: @Composable () -> Unit) {
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

    private fun fake(name: String): SavableSearchable = mockk<SavableSearchable>(relaxed = true).also {
        every { it.key } returns name
        every { it.label } returns name
    }

    @Test
    fun results() = shoot("s2-results") {
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
            ListResults(
                title = "The web", key = "web",
                items = listOf(fake("Search the web for mar")),
                itemContent = { item, _, _ -> Text(item.label, Modifier.padding(16.dp)) },
            )
        }
    }

    @Test
    fun settings() = shoot("s2-settings") {
        CompositionLocalProvider(LocalBackStack provides mockk(relaxed = true)) {
            PreferenceScreen(title = { Text("Search") }) {
                item {
                    PreferenceCategory("Appearance") {
                        Preference(title = "Theme", summary = "Colours, shapes, fonts", onClick = {})
                        Preference(title = "Animations", summary = "Full: orb, reactions and the ring light", onClick = {})
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
                        Preference(title = "About", onClick = {})
                    }
                }
            }
        }
    }

    @Test
    fun dialogLongText() = shoot("s2-dialog-long") {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            DialogPanel(
                title = { Text("Hide this item?") },
                text = {
                    Text(
                        (1..40).joinToString(" ") { "It will no longer show up in search results ($it)." },
                    )
                },
                dismissButton = { OutlinedButton(onClick = {}) { Text("Cancel") } },
                confirmButton = { Button(onClick = {}) { Text("Hide") } },
            )
        }
    }

    @Test
    fun sheet() = shoot("s2-sheet") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
                // content with NO fillMaxWidth: the sheet must still be full width
                SheetBody { Text("Restore backup", Modifier.padding(16.dp)) }
            }
        }
    }

    @Test
    fun widgetPage() = shoot("s2-widget-page") {
        Column(Modifier.padding(16.dp)) {
            LauncherCard(Modifier.padding(bottom = 12.dp)) { Text("Today: 10:24", Modifier.padding(16.dp)) }
            LauncherCard(Modifier.padding(bottom = 12.dp)) { Text("Calendar: Market day, 10:00", Modifier.padding(16.dp)) }
            // transparent card: must show NO frame
            LauncherCard(Modifier.padding(bottom = 12.dp), backgroundOpacity = 0f) {
                Text("Transparent card (no frame)", Modifier.padding(16.dp))
            }
        }
    }

    private fun shoot(name: String, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { Stage(content) }
        rule.mainClock.advanceTimeBy(1500)
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }
}
