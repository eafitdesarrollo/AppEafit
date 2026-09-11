package co.edu.eafit.appeafit.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.util.UiState
import co.edu.eafit.appeafit.domain.model.AppNotification
import co.edu.eafit.appeafit.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(private val container: AppContainer, private val user: User) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val state: StateFlow<UiState<List<AppNotification>>> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val result = container.notificationRepository.listFor(user.uid, user.role.id)
            result.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "No se pudieron cargar las notificaciones") }
        }
    }

    fun markRead(id: String) {
        viewModelScope.launch { container.notificationRepository.markRead(id) }
    }
}
