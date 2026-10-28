package de.mm20.launcher2.ui.medieval

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import de.mm20.launcher2.notifications.Notification
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * T18 screenshots of the People panel (typing layer) with unread counts, plus a check of the matching rules.
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*ContactAlertShots*" -i | grep "medieval shot"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ContactAlertShots {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun person(name: String, key: String, phone: String = "") =
        Person(name, Uri.parse("content://contacts/lookup/$key"), null, key, if (phone.isEmpty()) emptySet() else setOf(phone))

    private val people = listOf(
        person("Anna Becker", "a", "15550001"),
        person("Bartholomew-Maximilian Oberhauser", "b"),
        person("Cora", "c"),
        person("Dmitri", "d"),
        person("Eve", "e"),
    )

    private fun msg(id: Int, title: String, extras: Bundle = Bundle(), time: Long = 1000L + id) =
        Notification(
            id = id, key = "k$id", packageName = "p", postTime = time, isClearable = true, canShowBadge = true,
            number = 0, color = 0, smallIcon = null,
            extras = extras.apply { putCharSequence(android.app.Notification.EXTRA_TITLE, title); putCharSequence(android.app.Notification.EXTRA_TEXT, "x") },
            contentIntent = null, category = "msg",
        )

    private fun shoot(name: String, palette: Palette, notifications: List<Notification>) {
        Medieval.palette = palette
        StarredForTests = { people }
        val vm = io.mockk.mockk<CommandLayerVM>(relaxed = true).also {
            every { it.rankedPackages } returns MutableStateFlow(emptyList())
            every { it.hasContacts } returns MutableStateFlow(true)
            every { it.notifications } returns MutableStateFlow(notifications)
        }
        val owner = object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
            override val viewModelStore = ViewModelStore()
            override val defaultViewModelProviderFactory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = vm as T
            }
        }
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                Box(Modifier.fillMaxSize().background(Color(0xFF0B0907))) { CommandLayerContent() }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/T18").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
        Medieval.palette = Palettes.GoldOxblood
    }

    private val one = listOf(msg(1, "Anna Becker"))
    private val many = (1..12).map { msg(it, "Anna Becker") } + (20..22).map { msg(it, "Bartholomew-Maximilian Oberhauser") } + msg(30, "Eve")

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun portraitGold0() = shoot("portrait-gold-0", Palettes.GoldOxblood, emptyList())
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun portraitGold1() = shoot("portrait-gold-1", Palettes.GoldOxblood, one)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun portraitGold12() = shoot("portrait-gold-12", Palettes.GoldOxblood, many)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun portraitEmber12() = shoot("portrait-ember-12", Palettes.Ember, many)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeGold12() = shoot("landscape-gold-12", Palettes.GoldOxblood, many)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeEmber1() = shoot("landscape-ember-1", Palettes.Ember, one)

    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun matching() {
        val ids = people.map { ContactIdentity(it.name, it.lookupKey, it.phoneDigits) }
        val viaPerson = msg(40, "Group chat", Bundle().apply {
            putParcelableArrayList(
                "android.people.list",
                arrayListOf(android.app.Person.Builder().setUri("content://com.android.contacts/contacts/lookup/c/7").build()),
            )
        })
        val viaTel = msg(41, "+1 555 0001 caller", Bundle().apply {
            putParcelableArrayList("android.people.list", arrayListOf(android.app.Person.Builder().setUri("tel:+1 (555) 0001").build()))
        })
        val notAMessage = msg(42, "Dmitri").let { Notification(it.id, it.key, it.packageName, it.postTime, true, true, 0, 0, null, it.extras, 0, null, "email") }
        val missed = msg(43, "Dmitri").let { Notification(it.id, it.key, it.packageName, it.postTime, true, true, 0, 0, null, it.extras, 0, null, "missed_call") }
        val r = contactAlerts(ids, listOf(viaPerson, viaTel, notAMessage, missed, msg(44, "Stranger")))
        assertEquals(1, r["c"]?.size)
        assertEquals(1, r["a"]?.size)
        assertEquals(1, r["d"]?.size) // missed call yes, e-mail no
        assertEquals(null, r["e"])
    }
}
