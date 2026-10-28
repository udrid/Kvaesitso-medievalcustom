package de.mm20.launcher2.ui.medieval

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log

/** S5: the categories of the apps hub, in the order they are shown. */
internal enum class AppCategory(val title: String) {
    Communication("Communication"),
    Social("Social"),
    Media("Media and Music"),
    Games("Games"),
    Tools("Tools"),
    Shopping("Shopping"),
    Finance("Finance"),
    Travel("Travel and Maps"),
    Work("Work"),
    System("System"),
    Other("Other"),
}

internal class HubApp(
    val packageName: String,
    val label: String,
    /** `ApplicationInfo.category`, or -1 when the app did not say. */
    val androidCategory: Int = ApplicationInfo.CATEGORY_UNDEFINED,
    val isSystem: Boolean = false,
)

/**
 * S5 category rules, first match wins: (1) the owner's own move (long-press > Move to category),
 * (2) the shipped package list, (3) the category the app declares for itself, (4) system apps, (5) Other.
 */
internal object AppCategories {
    private val shipped: Map<String, AppCategory> = buildMap {
        fun add(c: AppCategory, vararg p: String) = p.forEach { put(it, c) }
        add(AppCategory.Communication,
            "com.google.android.apps.messaging", "com.samsung.android.messaging", "com.android.mms",
            "com.samsung.android.dialer", "com.google.android.dialer", "com.android.dialer",
            "com.samsung.android.contacts", "com.google.android.contacts", "com.android.contacts",
            "com.google.android.gm", "com.samsung.android.email.provider", "com.microsoft.office.outlook",
            "org.thoughtcrime.securesms", "org.telegram.messenger", "com.whatsapp", "com.discord",
            "com.google.android.apps.tachyon", "us.zoom.videomeetings", "com.skype.raider",
            "com.microsoft.teams", "com.Slack", "com.viber.voip", "com.facebook.orca")
        add(AppCategory.Social,
            "com.twitter.android", "com.instagram.android", "com.facebook.katana", "com.reddit.frontpage",
            "com.snapchat.android", "com.zhiliaoapp.musically", "com.linkedin.android",
            "com.pinterest", "com.tumblr", "org.joinmastodon.android")
        add(AppCategory.Media,
            "com.google.android.youtube", "com.google.android.apps.youtube.music", "com.spotify.music",
            "com.netflix.mediaclient", "com.amazon.avod.thirdpartyclient", "com.disney.disneyplus",
            "tv.twitch.android.app", "org.videolan.vlc", "com.google.android.videos",
            "com.audible.application", "com.sec.android.gallery3d", "com.google.android.apps.photos",
            "com.sec.android.app.camera", "com.samsung.android.app.camera")
        add(AppCategory.Games,
            "com.mojang.minecraftpe", "com.roblox.client", "com.nianticlabs.pokemongo",
            "com.innersloth.spacemafia", "com.samsung.android.game.gamehome")
        add(AppCategory.Tools,
            "com.sec.android.app.popupcalculator", "com.google.android.calculator",
            "com.sec.android.app.clockpackage", "com.google.android.deskclock",
            "com.sec.android.app.myfiles", "com.google.android.apps.nbu.files",
            "com.samsung.android.app.notes", "com.google.android.keep", "com.sec.android.app.voicenote",
            "com.android.chrome", "com.sec.android.app.sbrowser", "org.mozilla.firefox", "com.brave.browser",
            "com.duckduckgo.mobile.android", "com.google.android.apps.translate")
        add(AppCategory.Shopping,
            "com.amazon.mShop.android.shopping", "com.ebay.mobile", "com.etsy.android",
            "com.walmart.android", "com.target.ui", "com.alibaba.aliexpresshd")
        add(AppCategory.Finance,
            "com.paypal.android.p2pmobile", "com.venmo", "com.squareup.cash", "com.chase.sig.android",
            "com.infonow.bofa", "com.wf.wellsfargomobile", "com.coinbase.android",
            "com.google.android.apps.walletnfcrel", "com.samsung.android.spay", "com.robinhood.android")
        add(AppCategory.Travel,
            "com.google.android.apps.maps", "com.waze", "com.ubercab", "com.lyft.android",
            "com.airbnb.android", "com.tripadvisor.tripadvisor", "com.expedia.bookings", "com.here.app.maps")
        add(AppCategory.Work,
            "com.google.android.apps.docs", "com.google.android.apps.docs.editors.docs",
            "com.google.android.apps.docs.editors.sheets", "com.google.android.apps.docs.editors.slides",
            "com.microsoft.office.word", "com.microsoft.office.excel", "com.microsoft.office.powerpoint",
            "com.microsoft.office.officehubrow", "com.microsoft.skydrive", "com.dropbox.android",
            "com.notion.id", "com.todoist", "com.samsung.android.calendar", "com.google.android.calendar",
            "com.adobe.reader", "com.google.android.apps.tasks", "com.samsung.android.app.reminder")
        add(AppCategory.System,
            "com.android.settings", "com.android.vending", "com.sec.android.app.samsungapps",
            "com.google.android.googlequicksearchbox", "com.samsung.android.bixby.agent",
            "com.samsung.android.themestore", "com.google.android.apps.wellbeing")
    }

    private val gamePrefixes = listOf("com.supercell.", "com.king.", "com.activision.", "com.epicgames.", "com.ea.gp.")

    /** The category the app declares for itself (Android 8+), or null when undefined or unhelpful. */
    private fun fromAndroid(category: Int): AppCategory? = when (category) {
        ApplicationInfo.CATEGORY_GAME -> AppCategory.Games
        ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> AppCategory.Media
        ApplicationInfo.CATEGORY_SOCIAL, ApplicationInfo.CATEGORY_NEWS -> AppCategory.Social
        ApplicationInfo.CATEGORY_MAPS -> AppCategory.Travel
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.Work
        ApplicationInfo.CATEGORY_ACCESSIBILITY -> AppCategory.System
        else -> null // CATEGORY_UNDEFINED (-1)
    }

    fun categoryOf(app: HubApp, moved: Map<String, AppCategory>): AppCategory =
        moved[app.packageName]
            ?: shipped[app.packageName]
            ?: gamePrefixes.firstOrNull { app.packageName.startsWith(it) }?.let { AppCategory.Games }
            ?: fromAndroid(app.androidCategory)
            ?: if (app.isSystem) AppCategory.System else AppCategory.Other

    /** Apps by category, A to Z inside each; empty categories are left out. */
    fun group(apps: List<HubApp>, moved: Map<String, AppCategory>): Map<AppCategory, List<HubApp>> =
        apps.groupBy { categoryOf(it, moved) }
            .mapValues { (_, v) -> v.sortedBy { it.label.lowercase() } }
            .toSortedMap(compareBy { it.ordinal })

    /** Launchable apps (the launcher itself left out). */
    fun installed(context: Context): List<HubApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return try {
            pm.queryIntentActivities(intent, 0)
                .filter { it.activityInfo.packageName != context.packageName }
                .map {
                    val info = it.activityInfo.applicationInfo
                    HubApp(
                        it.activityInfo.packageName, it.loadLabel(pm).toString(),
                        info.category, (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    )
                }
                .distinctBy { it.packageName }
        } catch (e: Exception) {
            Log.e("medieval", "Apps hub: could not list the installed apps", e)
            emptyList()
        }
    }
}

/** The owner's own sorting and starred apps (SharedPreferences, same file as the other Home settings). */
internal object HubPrefs {
    private const val Prefs = "medieval_home"
    private const val MovedPrefix = "hub_cat_"
    private const val StarKey = "hub_favourites"

    private fun prefs(c: Context) = c.getSharedPreferences(Prefs, Context.MODE_PRIVATE)

    fun moved(c: Context): Map<String, AppCategory> = prefs(c).all.mapNotNull { (k, v) ->
        if (!k.startsWith(MovedPrefix) || v !is String) return@mapNotNull null
        val cat = AppCategory.entries.firstOrNull { it.name == v } ?: return@mapNotNull null
        k.removePrefix(MovedPrefix) to cat
    }.toMap()

    fun move(c: Context, pkg: String, category: AppCategory?) {
        val e = prefs(c).edit()
        if (category == null) e.remove(MovedPrefix + pkg) else e.putString(MovedPrefix + pkg, category.name)
        e.apply()
    }

    fun stars(c: Context): List<String> =
        prefs(c).getString(StarKey, "")!!.split(',').filter { it.isNotEmpty() }

    fun setStar(c: Context, pkg: String, starred: Boolean) {
        val now = stars(c).filter { it != pkg } + if (starred) listOf(pkg) else emptyList()
        prefs(c).edit().putString(StarKey, now.joinToString(",")).apply()
    }
}
