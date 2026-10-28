package de.mm20.launcher2.ui.medieval

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import de.mm20.launcher2.devicepose.DevicePoseProvider
import de.mm20.launcher2.preferences.LatLon
import de.mm20.launcher2.preferences.weather.WeatherSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.calendar.CalendarRepository
import de.mm20.launcher2.notifications.Notification
import de.mm20.launcher2.notifications.NotificationRepository
import de.mm20.launcher2.permissions.PermissionGroup
import de.mm20.launcher2.permissions.PermissionsManager
import de.mm20.launcher2.search.CalendarEvent
import de.mm20.launcher2.weather.Forecast
import de.mm20.launcher2.weather.WeatherRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

enum class WeatherStatus { SetUp, AllowLocation, Fetching, Unavailable, NoCoverage, Ready }

/** Data for the Deliverance Home screen that comes from Kvaesitso's own repositories. */
class MedievalHomeVM : ViewModel(), KoinComponent {
    private val weatherRepository: WeatherRepository by inject()
    private val calendarRepository: CalendarRepository by inject()
    val permissionsManager: PermissionsManager by inject()

    /** Weather right now, or null when no weather provider or location is set up. */
    var forecast by mutableStateOf<Forecast?>(null)
        private set

    /** S6: hourly forecasts from an hour ago on, oldest first (what the Forecast strip shows); empty when none. */
    var hourly by mutableStateOf<List<Forecast>>(emptyList())
        private set

    /** Upcoming events from now on, soonest first. */
    var events by mutableStateOf<List<CalendarEvent>>(emptyList())
        private set

    val hasCalendarPermission = permissionsManager.hasPermission(PermissionGroup.Calendar)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val hasNotificationAccess = permissionsManager.hasPermission(PermissionGroup.Notifications)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val notificationRepository: NotificationRepository by inject()

    /** Active notifications, unfiltered; [Dispatches.kt] decides what counts as a dispatch. */
    val notifications = notificationRepository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /** Dismisses the real notification on the phone. */
    fun dismissNotification(notification: Notification) {
        notificationRepository.cancelNotification(notification)
    }

    private val weatherSettings: WeatherSettings by inject()
    private val devicePoseProvider: DevicePoseProvider by inject()

    /** What the weather spot under the clock should say; see [WeatherStatus]. */
    var weatherStatus by mutableStateOf(WeatherStatus.Fetching)
        private set

    private var haveFix = false

    private val hasLocationPermission = permissionsManager.hasPermission(PermissionGroup.Location)

    init {
        viewModelScope.launch {
            weatherRepository.getDailyForecasts().collectLatest { days ->
                val now = System.currentTimeMillis()
                hourly = days.flatMap { it.hourlyForecasts }
                    .filter { it.timestamp >= now - 60 * 60_000L }
                    .sortedBy { it.timestamp }
                    .distinctBy { it.timestamp }
                    .take(48)
                val day = days.lastOrNull { it.timestamp < now } ?: days.firstOrNull()
                forecast = day?.hourlyForecasts?.lastOrNull { it.timestamp < now }
                    ?: day?.hourlyForecasts?.firstOrNull()
            }
        }
        viewModelScope.launch {
            combine(
                weatherRepository.getActiveProvider().map { it != null },
                weatherSettings.autoLocation,
                hasLocationPermission,
                snapshotFlow { forecast != null },
            ) { hasProvider, auto, permitted, hasForecast ->
                when {
                    hasForecast -> WeatherStatus.Ready
                    !hasProvider -> WeatherStatus.SetUp
                    auto && !permitted -> WeatherStatus.AllowLocation
                    else -> WeatherStatus.Fetching
                }
            }.distinctUntilChanged().collectLatest { status ->
                weatherStatus = status
                if (status == WeatherStatus.Fetching) {
                    // The background job reports nothing back, so give it a minute, then say so.
                    delay(60_000)
                    Log.e("medieval", "Weather: no forecast after 60 s")
                    // Bright Sky ("dwd") only answers near a DWD station: with a fix and no data, say so.
                    weatherStatus = if (haveFix && weatherSettings.providerId.first() == "dwd") WeatherStatus.NoCoverage else WeatherStatus.Unavailable
                }
            }
        }
        viewModelScope.launch {
            // The background update job gets no live GPS fix without background-location permission.
            // Get one here (the launcher is on screen) and store it; that also triggers an update.
            combine(weatherSettings.autoLocation, hasLocationPermission) { auto, permitted -> auto && permitted }
                .distinctUntilChanged()
                .collectLatest { wanted ->
                    if (!wanted) return@collectLatest
                    val fix = withTimeoutOrNull(30_000) {
                        devicePoseProvider.getLocation(skipCache = true).firstOrNull()
                    }
                    if (fix == null) {
                        Log.e("medieval", "Weather: no location fix within 30 s (is Android location on?)")
                    } else {
                        haveFix = true
                        weatherSettings.setLastLocation(LatLon(fix.latitude, fix.longitude))
                    }
                }
        }
        viewModelScope.launch {
            val from = System.currentTimeMillis()
            calendarRepository.findMany(
                from = from,
                to = from + 14 * 24 * 60 * 60 * 1000L,
            ).collectLatest { found ->
                events = found
                    .filter { !it.isTask }
                    .sortedBy { it.startTime ?: it.endTime }
            }
        }
    }
}
