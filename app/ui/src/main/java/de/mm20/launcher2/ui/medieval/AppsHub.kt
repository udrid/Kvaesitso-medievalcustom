package de.mm20.launcher2.ui.medieval

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What the Today strip shows; null parts are simply left out. */
internal class HubToday(
    val eventTime: String?,
    val eventTitle: String?,
    /** `needsCalendar` is true while calendar access is missing (the strip then says how to grant it). */
    val needsCalendar: Boolean,
    val weather: String?,
)

/** Screenshot tests put a fixed list of apps here (the test phone has none). */
internal var HubAppsForTests: (() -> List<HubApp>)? = null

/**
 * S5: the swipe-up page's top part, wired to the real phone (installed apps, calendar, weather).
 * Sits above the widget column in `WidgetsComponent`.
 */
@Composable
internal fun AppsHubHost(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val vm: MedievalHomeVM = viewModel()
    var tick by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { tick++ }
    var apps by remember { mutableStateOf<List<HubApp>>(emptyList()) }
    LaunchedEffect(tick) {
        apps = HubAppsForTests?.invoke() ?: withContext(Dispatchers.Default) { AppCategories.installed(context) }
    }
    val hasCalendar by vm.hasCalendarPermission.collectAsState()
    val now = System.currentTimeMillis()
    val next = vm.events.firstOrNull { it.endTime >= now }
    val today = HubToday(
        eventTime = next?.let { timeLabel(context, it) },
        eventTitle = next?.label,
        needsCalendar = hasCalendar == false,
        weather = vm.forecast?.let { weatherTemp(it) + it.condition },
    )
    AppsHub(apps, today, tick, modifier)
}

/** The hub itself: search bar, favourites, category tiles (one open at a time), Today strip. */
@Composable
internal fun AppsHub(apps: List<HubApp>, today: HubToday, tick: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var query by rememberSaveable { mutableStateOf("") }
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    val moved = remember(tick, revision) { HubPrefs.moved(context) }
    val stars = remember(tick, revision) { HubPrefs.stars(context) }
    val groups = remember(apps, moved) { AppCategories.group(apps, moved) }
    val byPackage = remember(apps) { apps.associateBy { it.packageName } }
    var menuFor by remember { mutableStateOf<String?>(null) }
    var moving by remember { mutableStateOf(false) }

    // favourites: the owner's stars; until there are some, the launcher's most used apps
    val favourites = remember(tick, revision, stars, apps) {
        val starred = stars.mapNotNull { byPackage[it] }
        if (starred.isNotEmpty()) starred else UsageCounter.top(context, 12).mapNotNull { byPackage[it] }.take(5)
    }
    val favTitle = if (stars.any { byPackage.containsKey(it) }) "Favourites" else "Most used"

    @Composable
    fun Cell(app: HubApp, m: Modifier) {
        Box(m) {
            AppCell(app.packageName, Modifier.fillMaxWidth().padding(vertical = 4.dp), app.label) {
                menuFor = app.packageName; moving = false
            }
            if (menuFor == app.packageName) {
                DropdownMenu(expanded = true, onDismissRequest = { menuFor = null; moving = false }) {
                    if (!moving) {
                        HubMenuItem("Open") {
                            menuFor = null
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                                ?.let { launchSafely(context, it, app.label) }
                        }
                        HubMenuItem("Move to category...") { moving = true }
                        val starred = stars.contains(app.packageName)
                        HubMenuItem(if (starred) "Remove from favourites" else "Add to favourites") {
                            HubPrefs.setStar(context, app.packageName, !starred); revision++; menuFor = null
                        }
                        HubMenuItem("App info") {
                            menuFor = null
                            launchSafely(
                                context,
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.packageName}")),
                                "app info",
                            )
                        }
                    } else {
                        val current = AppCategories.categoryOf(app, moved)
                        AppCategory.entries.forEach { c ->
                            HubMenuItem(c.title + if (c == current) "  *" else "") {
                                HubPrefs.move(context, app.packageName, c); revision++; menuFor = null; moving = false
                            }
                        }
                        if (moved.containsKey(app.packageName)) {
                            HubMenuItem("Automatic") {
                                HubPrefs.move(context, app.packageName, null); revision++; menuFor = null; moving = false
                            }
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
        val wide = maxWidth >= 600.dp
        val appCols = if (wide) 6 else 4
        val tileCols = if (wide) 3 else 2
        Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            HubSearchBar(query) { query = it }
            val q = query.trim()
            if (q.isNotEmpty()) {
                val found = apps.filter { it.label.contains(q, ignoreCase = true) }.sortedBy { it.label.lowercase() }
                HubPanel("Found - ${found.size}") {
                    if (found.isEmpty()) MText("No app by that name", 13f, Medieval.TextDim, italic = true)
                    else found.chunked(appCols).forEach { row ->
                        Row(Modifier.fillMaxWidth()) {
                            row.forEach { Cell(it, Modifier.weight(1f)) }
                            repeat(appCols - row.size) { Box(Modifier.weight(1f)) }
                        }
                    }
                }
            } else {
                if (favourites.isNotEmpty()) HubPanel(favTitle) {
                    Row(Modifier.fillMaxWidth()) {
                        favourites.take(appCols).forEach { Cell(it, Modifier.weight(1f)) }
                        repeat(appCols - favourites.take(appCols).size) { Box(Modifier.weight(1f)) }
                    }
                }
                HubPanel("The Armoury - ${apps.size}") {
                    val cats = groups.keys.toList()
                    cats.chunked(tileCols).forEach { row ->
                        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { c ->
                                CategoryTile(c, groups.getValue(c).size, open == c.name, Modifier.weight(1f)) {
                                    open = if (open == c.name) null else c.name
                                }
                            }
                            repeat(tileCols - row.size) { Box(Modifier.weight(1f)) }
                        }
                        val openHere = row.firstOrNull { it.name == open }
                        if (openHere != null) {
                            Column(
                                Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    .background(Medieval.PanelBottom)
                                    .border(1.dp, Medieval.GoldDeep)
                                    .padding(horizontal = 4.dp, vertical = 8.dp),
                            ) {
                                groups.getValue(openHere).chunked(appCols).forEach { r ->
                                    Row(Modifier.fillMaxWidth()) {
                                        r.forEach { Cell(it, Modifier.weight(1f)) }
                                        repeat(appCols - r.size) { Box(Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    }
                    if (apps.isEmpty()) MText("No apps found", 13f, Medieval.TextDim, italic = true)
                }
                TodayStrip(today)
            }
        }
    }
}

@Composable
private fun HubMenuItem(text: String, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text.uppercase(),
                style = TextStyle(
                    fontFamily = Medieval.Cinzel, fontWeight = FontWeight.Bold, fontSize = 9.sp,
                    letterSpacing = 1.4.sp, color = Medieval.Text,
                ),
            )
        },
        onClick = onClick,
    )
}

/** Double-framed panel with the red ribbon heading drawn after the frame (no gold line through it). */
@Composable
private fun HubPanel(ribbon: String, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxWidth().padding(top = 9.dp)) {
        Column(
            Modifier.fillMaxWidth().medievalPanelBackground().medievalFrame()
                .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
            content = content,
        )
        Ribbon(ribbon, Modifier.offset(12.dp, (-8).dp))
    }
}

@Composable
private fun HubSearchBar(query: String, onChange: (String) -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(42.dp).medievalPanelBackground().medievalFrame().padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (query.isEmpty()) MText("Search your apps...", 15f, Medieval.TextFaint, italic = true)
        BasicTextField(
            value = query,
            onValueChange = onChange,
            singleLine = true,
            textStyle = TextStyle(color = Medieval.Text, fontSize = 15.sp, fontFamily = Medieval.FellEnglish),
            cursorBrush = SolidColor(Medieval.Gold),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val CategoryGlyphs = mapOf(
    AppCategory.Communication to listOf("M3.5 6h17v12h-17z", "M3.5 6.5l8.5 6.2 8.5-6.2"),
    AppCategory.Social to listOf("M3 14c3-1 5-3 6-6 2 1 4 1 6 0l5 3-4 1c0 4-3 7-8 7H6l2-3-5-2z"),
    AppCategory.Media to listOf(svgCircle(9f, 15f, 5f), "M12.5 11.5L20 4M18 3l3 3"),
    AppCategory.Games to listOf("M12 3v14M8 14h8M12 17v4"),
    AppCategory.Tools to listOf(svgCircle(12f, 12f, 3f), "M12 3v3M12 18v3M3 12h3M18 12h3M5.6 5.6l2.1 2.1M16.3 16.3l2.1 2.1M18.4 5.6l-2.1 2.1M7.7 16.3l-2.1 2.1"),
    AppCategory.Shopping to listOf("M5 8h14l-1 12H6z", "M9 8V6a3 3 0 0 1 6 0v2"),
    AppCategory.Finance to listOf(svgCircle(12f, 12f, 8f), svgCircle(12f, 12f, 4f)),
    AppCategory.Travel to listOf(svgCircle(12f, 12f, 9f), "M12 5.2l1.8 5 5 1.8-5 1.8-1.8 5-1.8-5-5-1.8 5-1.8z"),
    AppCategory.Work to listOf("M6 6.5h12v11H6z", "M3.6 6.2a1.4 1.4 0 0 1 2.8 0v11.6a1.4 1.4 0 0 1-2.8 0z", "M17.6 6.2a1.4 1.4 0 0 1 2.8 0v11.6a1.4 1.4 0 0 1-2.8 0z"),
    AppCategory.System to listOf("M6 21V8l2-2V3h2v2h4V3h2v3l2 2v13z", "M10 21v-5h4v5"),
    AppCategory.Other to listOf(svgCircle(7f, 12f, 1.6f), svgCircle(12f, 12f, 1.6f), svgCircle(17f, 12f, 1.6f)),
)

@Composable
private fun CategoryTile(category: AppCategory, count: Int, open: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val edge = if (open) Medieval.Gold else Medieval.GoldDark
    Row(
        modifier.height(50.dp)
            .medievalPanelBackground()
            .border(1.dp, edge)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SvgIcon(CategoryGlyphs.getValue(category), 18f, if (open) Medieval.Gold else Medieval.GoldDeep)
        Box(Modifier.width(8.dp))
        MText(
            category.title.uppercase(), 8.5f, if (open) Medieval.TextBright else Medieval.Text, Modifier.weight(1f),
            font = Medieval.Cinzel, bold = true, spacingEm = 0.12f, maxLines = 2, lineHeight = 11f,
        )
        MText(count.toString(), 9f, Medieval.GoldDeep, font = Medieval.Cinzel, bold = true)
    }
}

/** Next calendar event and the weather, one compact framed strip (data already on the phone, no new permission). */
@Composable
private fun TodayStrip(today: HubToday) {
    HubPanel("Today") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Cap("Next on the Ledger", size = 8.5f, color = Medieval.Label)
                Box(Modifier.height(3.dp))
                when {
                    today.needsCalendar -> MText("Calendar access is off", 13f, Medieval.TextDim, italic = true)
                    today.eventTitle == null -> MText("No tidings", 13f, Medieval.TextDim, italic = true)
                    else -> MText(
                        listOfNotNull(today.eventTime?.takeIf { it.isNotEmpty() }, today.eventTitle).joinToString("  "),
                        13f, Medieval.TextSoft,
                    )
                }
            }
            if (today.weather != null) {
                Box(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Cap("Weather", size = 8.5f, color = Medieval.Label)
                    Box(Modifier.height(3.dp))
                    MText(today.weather, 13f, Medieval.WeatherGrey, italic = true)
                }
            }
        }
    }
}
