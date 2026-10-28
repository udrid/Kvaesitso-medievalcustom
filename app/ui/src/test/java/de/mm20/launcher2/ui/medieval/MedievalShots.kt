package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.search.CalendarEvent
import de.mm20.launcher2.ui.launcher.scaffold.LauncherScaffoldState
import de.mm20.launcher2.ui.launcher.search.SearchVM
import de.mm20.launcher2.weather.Forecast
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.util.Calendar

/*
 * Deliverance screenshot tests (R1, ops/emulator.md): render the Home stage on the PC with fake data and
 * S20 FE-like insets, write PNGs to docs/design/shots/. Not a pass/fail test: look at the pictures.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*MedievalShots*"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class MedievalShots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** S20 FE: 1080 x 2400 px at 2.625 (411 x 914 dp); 3-button bar 48 dp; hole cutout 32 dp deep. */
    private val navPx = 126
    private val cutPx = 84

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun portrait() = shoot("home-portrait", Side.Bottom, Side.Top)

    /** Phone turned left (top of the phone to the left): camera on the left, buttons on the right. */
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeLeft() = shoot("home-landscape-camera-left", Side.Right, Side.Left)

    /** Phone turned right: camera on the right, buttons on the left. */
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeRight() = shoot("home-landscape-camera-right", Side.Left, Side.Right)

    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeTyping() = shoot("home-landscape-typing", Side.Right, Side.Left, openCommand = true)

    @Test @Config(qualifiers = "w640dp-h360dp-land-xhdpi")
    fun smallLandscape() = shoot("home-small-640x360-landscape", Side.Right, Side.Left)

    @Test @Config(qualifiers = "w360dp-h640dp-port-xhdpi")
    fun smallPortrait() = shoot("home-small-360x640-portrait", Side.Bottom, Side.Top)

    private enum class Side { Left, Top, Right, Bottom }

    private fun shoot(name: String, nav: Side, camera: Side, openCommand: Boolean = false) {
        val owner = FakeOwner()
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
        // mark where the phone's own parts sit: navigation bar (blue) and camera hole (red)
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
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
    }

    /** Hands out fakes instead of the real view models (they need Koin and the phone's repositories). */
    internal class FakeOwner : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
        override val viewModelStore = ViewModelStore()
        override val defaultViewModelProviderFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = when (modelClass) {
                MedievalHomeVM::class.java -> fakeHome()
                SearchVM::class.java -> mockk<SearchVM>(relaxed = true).also {
                    every { it.searchQuery } returns androidx.compose.runtime.mutableStateOf("")
                }
                CommandLayerVM::class.java -> mockk<CommandLayerVM>(relaxed = true).also {
                    every { it.rankedPackages } returns MutableStateFlow(emptyList())
                    every { it.hasContacts } returns MutableStateFlow(false)
                    every { it.notifications } returns MutableStateFlow(emptyList())
                }
                else -> io.mockk.mockkClass(modelClass.kotlin, relaxed = true)
            } as T
        }
    }
}

/** S6: true = a busy day (4 events, 6 forecast hours, 5 dispatches); false = the quiet default (2 events, nothing else). */
internal var fakeHomeFull = false

internal fun fakeHome(): MedievalHomeVM = mockk<MedievalHomeVM>(relaxed = true).also { vm ->
    val year = Calendar.getInstance().get(Calendar.YEAR) + 1
    fun day(m: Int, d: Int) = Calendar.getInstance().apply { set(year, m, d, 0, 0, 0) }.timeInMillis
    fun event(key: String, label: String, start: Long) = mockk<CalendarEvent>(relaxed = true).also {
        every { it.key } returns key
        every { it.label } returns label
        every { it.startTime } returns start
        every { it.endTime } returns start + 86_400_000L
        every { it.allDay } returns true
    }
    every { vm.events } returns (listOf(
        event("e1", "Halloween", day(Calendar.OCTOBER, 31)),
        event("e2", "Daylight Saving Time ends", day(Calendar.NOVEMBER, 1)),
    ) + if (!fakeHomeFull) emptyList() else listOf(
        event("e3", "Election Day", day(Calendar.NOVEMBER, 3)),
        event("e4", "Dentist, a very long appointment title", day(Calendar.NOVEMBER, 5)),
    ))
    every { vm.forecast } returns Forecast(
        timestamp = System.currentTimeMillis(),
        temperature = 290.4,
        icon = Forecast.PARTLY_CLOUDY,
        condition = "Partly cloudy",
        location = "Test",
        provider = "Test",
        updateTime = System.currentTimeMillis(),
    )
    every { vm.weatherStatus } returns WeatherStatus.Ready
    every { vm.hasCalendarPermission } returns MutableStateFlow(true)
    every { vm.hasNotificationAccess } returns MutableStateFlow(true)
    every { vm.notifications } returns MutableStateFlow(if (fakeHomeFull) fakeNotifications() else emptyList())
    val h = 3_600_000L
    val t0 = System.currentTimeMillis() / h * h
    every { vm.hourly } returns if (!fakeHomeFull) emptyList() else listOf(
        Forecast.CLEAR to 290.4, Forecast.PARTLY_CLOUDY to 289.2, Forecast.OVERCAST to 288.0,
        Forecast.RAIN to 286.5, Forecast.THUNDERSTORM to 285.5, Forecast.SNOW to 270.0,
    ).mapIndexed { i, (icon, k) ->
        Forecast(timestamp = t0 + (i + 1) * h, temperature = k, icon = icon, condition = "Test", location = "Test", provider = "Test", updateTime = t0, night = i >= 3)
    }
}

private fun fakeNotifications(): List<de.mm20.launcher2.notifications.Notification> {
    val now = System.currentTimeMillis()
    fun n(i: Int, title: String, text: String) = de.mm20.launcher2.notifications.Notification(
        id = i, key = "n$i", packageName = "test.app$i", postTime = now - i * 60_000L, isClearable = true,
        canShowBadge = true, number = 0, color = 0, smallIcon = null,
        extras = android.os.Bundle().apply {
            putCharSequence(android.app.Notification.EXTRA_TITLE, title)
            putCharSequence(android.app.Notification.EXTRA_TEXT, text)
        },
        contentIntent = null,
    )
    return listOf(
        n(1, "New voicemail", "0:12"), n(2, "[Sender]", "[message]"), n(3, "[Discord]", "[mention] with a long line of text"),
        n(4, "[Sender]", "[message]"), n(5, "[X]", "[reply]"), n(6, "[Extra]", "[hidden: sixth row]"),
    )
}
