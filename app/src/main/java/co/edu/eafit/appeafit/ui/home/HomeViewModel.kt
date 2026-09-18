package co.edu.eafit.appeafit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.CalendarEvent
import co.edu.eafit.appeafit.domain.model.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    val news: StateFlow<List<NewsItem>> = container.newsRepository.observeCached()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Corta el día en el instante 00:00 local, no en "ahora mismo": un evento de hoy
    // temprano en la mañana no debe desaparecer de "Próximos eventos" solo porque ya
    // pasó esa hora exacta -- sigue siendo relevante durante todo el día de hoy.
    val upcomingEvents: StateFlow<List<CalendarEvent>> = container.calendarRepository.observeCached()
        .map { events ->
            val startOfToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            events.filter { it.date >= startOfToday }.sortedBy { it.date }.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            container.newsRepository.refresh()
            container.calendarRepository.refresh()
            _isRefreshing.value = false
        }
    }
}
