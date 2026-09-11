package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.util.UiState
import co.edu.eafit.appeafit.domain.model.Reservation
import co.edu.eafit.appeafit.domain.model.ReservationStatus
import co.edu.eafit.appeafit.domain.model.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReservationViewModel(private val container: AppContainer, private val manageAll: Boolean) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<Reservation>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Reservation>>> = _state

    private val _spaces = MutableStateFlow<List<Space>>(emptyList())
    val spaces: StateFlow<List<Space>> = _spaces

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            container.reservationRepository.listSpaces().onSuccess { _spaces.value = it }
            val result = if (manageAll) container.reservationRepository.listAll() else container.reservationRepository.listForUser(userId)
            result.onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "No se pudieron cargar las reservas") }
        }
    }

    fun createReservation(userId: String, userName: String, space: Space, date: String, start: String, end: String, purpose: String) {
        viewModelScope.launch {
            container.reservationRepository.create(
                Reservation(
                    spaceId = space.id,
                    spaceName = space.name,
                    userId = userId,
                    userName = userName,
                    date = date,
                    startTime = start,
                    endTime = end,
                    purpose = purpose,
                    status = ReservationStatus.PENDING.id,
                    createdAt = System.currentTimeMillis()
                )
            )
            load(userId)
        }
    }

    fun updateStatus(id: String, status: String, refreshUserId: String) {
        viewModelScope.launch {
            container.reservationRepository.updateStatus(id, status)
            load(refreshUserId)
        }
    }
}
