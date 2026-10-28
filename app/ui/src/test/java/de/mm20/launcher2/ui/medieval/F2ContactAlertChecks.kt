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
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/*
 * F2 final check of T18: matching rules with fake notifications built by the real Notification.Builder,
 * and People panel pictures T18 did not take (halo playing, 99+, 360 dp wide, font 130 %).
 * Run: ./gradlew :app:ui:testDebugUnitTest --tests "*F2ContactAlertChecks*" -i | grep "medieval shot"
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class F2ContactAlertChecks {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val anna = ContactIdentity("Anna Becker", "a", setOf("15550001"))
    private val annaS = ContactIdentity("Anna Schmidt", "s", emptySet())
    private val cora = ContactIdentity("Cora", "c", emptySet())
    private val ids = listOf(anna, annaS, cora)

    private var nextId = 100

    /** A notification as the listener would see it, built by the real framework builder. */
    private fun posted(
        category: String? = android.app.Notification.CATEGORY_MESSAGE,
        channel: String = "chat",
        flags: Int = 0,
        clearable: Boolean = true,
        build: android.app.Notification.Builder.() -> Unit,
    ): Notification {
        val ctx = RuntimeEnvironment.getApplication()
        val n = android.app.Notification.Builder(ctx, channel)
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .apply { if (category != null) setCategory(category) }
            .apply(build)
            .build()
        val id = nextId++
        return Notification(
            id = id, key = "k$id", packageName = "p", postTime = 1000L + id, isClearable = clearable,
            canShowBadge = true, number = 0, color = 0, smallIcon = null, extras = n.extras,
            flags = n.flags or flags, contentIntent = null, category = n.category, channelId = n.channelId,
        )
    }

    private fun person(name: String, uri: String? = null) =
        android.app.Person.Builder().setName(name).apply { if (uri != null) setUri(uri) }.build()

    private fun counts(vararg n: Notification) =
        contactAlerts(ids, n.toList().asDispatches()).mapValues { it.value.size }

    @Test
    fun exactTitleMatchesOnlyThatContact() {
        val r = counts(posted { setContentTitle("anna becker"); setContentText("hi") })
        assertEquals(mapOf("a" to 1), r)
    }

    @Test
    fun partialOrFirstNameMatchesNobody() {
        // "Anna" is the first name of two starred contacts: neither may get it
        val r = counts(
            posted { setContentTitle("Anna"); setContentText("hi") },
            posted { setContentTitle("Anna Beck"); setContentText("hi") },
            posted { setContentTitle("Anna Becker Jr"); setContentText("hi") },
        )
        assertEquals(emptyMap<String, Int>(), r)
    }

    @Test
    fun sameFirstNameGoesToTheRightAnna() {
        val r = counts(
            posted { setContentTitle("Anna Schmidt"); setContentText("hi") },
            posted { setContentTitle("Anna Schmidt"); setContentText("again") },
            posted { setContentTitle("Anna Becker"); setContentText("hi") },
        )
        assertEquals(mapOf("s" to 2, "a" to 1), r)
    }

    @Test
    fun oneToOneMessagingStyleBySenderPerson() {
        // title is a nickname, sender Person carries Anna's number
        val r = counts(posted {
            setContentTitle("Mum")
            style = android.app.Notification.MessagingStyle(person("Me"))
                .addMessage("hi", 1L, person("Mum", "tel:+1-555-0001"))
        })
        assertEquals(mapOf("a" to 1), r)
    }

    @Test
    fun missedCallByCategoryAndByChannel() {
        val r = counts(
            posted(category = "missed_call") { setContentTitle("Cora"); setContentText("Missed call") },
            posted(category = null, channel = "phone_missed_call") { setContentTitle("Cora"); setContentText("Missed call") },
            posted(category = android.app.Notification.CATEGORY_CALL, channel = "incoming") { setContentTitle("Cora"); setContentText("Calling") },
        )
        assertEquals(mapOf("c" to 2), r)
    }

    @Test
    fun notAMessageNeverCounts() {
        val r = counts(
            posted(category = android.app.Notification.CATEGORY_EMAIL) { setContentTitle("Cora"); setContentText("mail") },
            posted(category = android.app.Notification.CATEGORY_EVENT) { setContentTitle("Cora"); setContentText("meeting") },
            posted(category = null, channel = "updates") { setContentTitle("Cora"); setContentText("update") },
        )
        assertEquals(emptyMap<String, Int>(), r)
    }

    @Test
    fun groupChatCountsOnlyTheNewestWriter() {
        // Anna wrote earlier and is a member; the newest message is from a stranger: no badge for anyone
        val strangerLast = posted {
            setContentTitle("Cora")  // a group named like a starred contact must not match either
            addPerson("tel:+15550001")
            style = android.app.Notification.MessagingStyle(person("Me"))
                .setConversationTitle("Cora").setGroupConversation(true)
                .addMessage("hello", 1L, person("Anna Becker"))
                .addMessage("hey", 2L, person("Zed"))
        }
        assertEquals(emptyMap<String, Int>(), counts(strangerLast))
        // newest from Anna: Anna only
        val annaLast = posted {
            setContentTitle("Family")
            addPerson(person("Cora"))
            style = android.app.Notification.MessagingStyle(person("Me"))
                .setConversationTitle("Family").setGroupConversation(true)
                .addMessage("hey", 1L, person("Cora"))
                .addMessage("hello", 2L, person("Anna Becker"))
        }
        assertEquals(mapOf("a" to 1), counts(annaLast))
    }

    @Test
    fun dismissedSummaryAndOngoingDoNotCount() {
        val live = posted { setContentTitle("Cora"); setContentText("hi") }
        val gone = posted { setContentTitle("Cora"); setContentText("hi 2") }
        assertEquals(mapOf("c" to 2), counts(live, gone))
        // the listener drops a removed notification from the list: the count follows at once
        assertEquals(mapOf("c" to 1), counts(live))
        val summary = posted(flags = android.app.Notification.FLAG_GROUP_SUMMARY) { setContentTitle("Cora"); setContentText("2 new") }
        val ongoing = posted(clearable = false) { setContentTitle("Cora"); setContentText("on call") }
        assertEquals(mapOf("c" to 1), counts(live, summary, ongoing))
    }

    // ---- pictures -------------------------------------------------------------------------------

    private fun p(name: String, key: String) = Person(name, Uri.parse("content://contacts/lookup/$key"), null, key, emptySet())
    private val people = listOf(
        p("Anna Becker", "a"), p("Bartholomew-Maximilian Oberhauser", "b"), p("Cora", "c"), p("Dmitri", "d"), p("Eve", "e"),
    )

    private fun msg(id: Int, title: String) = Notification(
        id = id, key = "k$id", packageName = "p", postTime = 1000L + id, isClearable = true, canShowBadge = true,
        number = 0, color = 0, smallIcon = null,
        extras = Bundle().apply { putCharSequence(android.app.Notification.EXTRA_TITLE, title); putCharSequence(android.app.Notification.EXTRA_TEXT, "x") },
        contentIntent = null, category = "msg",
    )

    private fun shoot(name: String, palette: Palette, first: List<Notification>, then: List<Notification>? = null, motionOn: Boolean = false, atMs: Long = 1500) {
        Medieval.palette = palette
        StarredForTests = { people }
        val flow = MutableStateFlow(first)
        val vm = mockk<CommandLayerVM>(relaxed = true).also {
            every { it.rankedPackages } returns MutableStateFlow(emptyList())
            every { it.hasContacts } returns MutableStateFlow(true)
            every { it.notifications } returns flow
        }
        val owner = object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
            override val viewModelStore = ViewModelStore()
            override val defaultViewModelProviderFactory = object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = vm as T
            }
        }
        val motion = Motion(if (motionOn) AnimLevel.Full else AnimLevel.Off, allowed = motionOn, charging = false, wakes = 0)
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner, LocalMotion provides motion) {
                Box(Modifier.fillMaxSize().background(Color(0xFF0B0907))) { CommandLayerContent() }
            }
        }
        rule.mainClock.advanceTimeBy(1500)
        if (then != null) {
            flow.value = then
            rule.mainClock.advanceTimeBy(atMs)
        }
        val bmp = rule.onRoot().captureToImage().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
        val dir = File(System.getProperty("medieval.shots") ?: "../../docs/design/shots/F2").apply { mkdirs() }
        val out = File(dir, "$name.png")
        out.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("medieval shot: ${out.canonicalPath} ${bmp.width}x${bmp.height}")
        Medieval.palette = Palettes.GoldOxblood
        StarredForTests = null
    }

    private val one = listOf(msg(1, "Anna Becker"))
    private val two = one + msg(2, "Anna Becker")
    private val many = (1..12).map { msg(it, "Anna Becker") } + (20..22).map { msg(it, "Bartholomew-Maximilian Oberhauser") } + msg(30, "Eve")
    private val lots = (1..120).map { msg(it, "Anna Becker") } + (200..211).map { msg(it, "Bartholomew-Maximilian Oberhauser") }

    // halo + swell mid-play (900 ms of 1.8 s) after a second message arrives, gate open
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun haloPlayingGold() = shoot("portrait-gold-halo", Palettes.GoldOxblood, one, two, motionOn = true, atMs = 900)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun haloPlayingEmber() = shoot("portrait-ember-halo", Palettes.Ember, one, two, motionOn = true, atMs = 900)
    // same arrival with the gate closed: static badge "2", no rings
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun gateClosedNoHalo() = shoot("portrait-gold-gate-closed", Palettes.GoldOxblood, one, two, motionOn = false, atMs = 900)
    // halo finished: nothing left behind
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun haloDone() = shoot("portrait-gold-halo-done", Palettes.GoldOxblood, one, two, motionOn = true, atMs = 2500)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi")
    fun badge99Plus() = shoot("portrait-gold-99plus", Palettes.GoldOxblood, lots)
    @Test @Config(qualifiers = "w914dp-h411dp-land-420dpi")
    fun landscapeEmber12() = shoot("landscape-ember-12", Palettes.Ember, many)
    @Test @Config(qualifiers = "w360dp-h640dp-port-420dpi")
    fun small360() = shoot("small-360x640-gold-12", Palettes.GoldOxblood, many)
    @Test @Config(qualifiers = "w411dp-h914dp-port-420dpi", fontScale = 1.3f)
    fun font130() = shoot("portrait-font130-gold-12", Palettes.GoldOxblood, many)
}
