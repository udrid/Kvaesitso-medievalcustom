package de.mm20.launcher2.ui.medieval

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.mm20.launcher2.calendar.CalendarRepository
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

/** Data for the Deliverance Home screen that comes from Kvaesitso's own repositories. */
class MedievalHomeVM : ViewModel(), KoinComponent {
    private val weatherRepository: WeatherRepository by inject()
    private val calendarRepository: CalendarRepository by inject()
    val permissionsManager: PermissionsManager by inject()

    /** Weather right now, or null when no weather provider or location is set up. */
    var forecast by mutableStateOf<Forecast?>(null)
        private set

    /** Upcoming events from now on, soonest first. */
    var events by mutableStateOf<List<CalendarEvent>>(emptyList())
        private set

    val hasCalendarPermission = permissionsManager.hasPermission(PermissionGroup.Calendar)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        viewModelScope.launch {
            weatherRepository.getDailyForecasts().collectLatest { days ->
                val now = System.currentTimeMillis()
                val day = days.lastOrNull { it.timestamp < now } ?: days.firstOrNull()
                forecast = day?.hourlyForecasts?.lastOrNull { it.timestamp < now }
                    ?: day?.hourlyForecasts?.firstOrNull()
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
