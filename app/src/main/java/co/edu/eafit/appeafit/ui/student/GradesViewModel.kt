package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Grade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GradesUiState(
    val grades: List<Grade> = emptyList(),
    val isRefreshing: Boolean = false
) {
    val averageByCourse: Map<String, Double>
        get() = grades.groupBy { it.courseName }.mapValues { (_, items) ->
            val totalWeight = items.sumOf { it.weightPercent }
            if (totalWeight <= 0.0) {
                items.map { it.score }.average()
            } else {
                items.sumOf { it.score * it.weightPercent } / totalWeight
            }
        }

    val overallAverage: Double
        get() = if (averageByCourse.isEmpty()) 0.0 else averageByCourse.values.average()
}

class GradesViewModel(container: AppContainer, studentId: String) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<GradesUiState> = kotlinx.coroutines.flow.combine(
        container.gradeRepository.observeCachedForStudent(studentId),
        _isRefreshing
    ) { grades, refreshing -> GradesUiState(grades, refreshing) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GradesUiState())

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            container.gradeRepository.refreshForStudent(studentId)
            _isRefreshing.value = false
        }
    }
}
