package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.util.UiState
import co.edu.eafit.appeafit.domain.model.LostItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LostItemsViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow<UiState<List<LostItem>>>(UiState.Loading)
    val state: StateFlow<UiState<List<LostItem>>> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            container.lostItemRepository.list()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "No se pudo cargar la lista") }
        }
    }

    fun report(title: String, description: String, location: String, reportedBy: String) {
        viewModelScope.launch {
            container.lostItemRepository.report(
                LostItem(
                    title = title,
                    description = description,
                    location = location,
                    reportedBy = reportedBy,
                    createdAt = System.currentTimeMillis()
                )
            )
            load()
        }
    }

    fun markClaimed(id: String) {
        viewModelScope.launch {
            container.lostItemRepository.updateStatus(id, "claimed")
            load()
        }
    }
}
