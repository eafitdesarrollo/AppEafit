package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Grade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GradesUiState(
    val grades: List<Grade> = emptyList(),
    val courses: List<Course> = emptyList(),
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

    /**
     * Ponderado por créditos de cada curso, igual que la calculadora de promedio manual
     * (GpaCalculatorScreen) — antes este promedio se calculaba como un simple promedio
     * de promedios por curso, ignorando créditos, lo que daba un número distinto al de
     * la calculadora para el mismo semestre.
     */
    val overallAverage: Double
        get() {
            val creditsByCourseName = courses.associate { it.name to it.credits }
            val totalCredits = averageByCourse.keys.sumOf { (creditsByCourseName[it] ?: 0).toDouble() }
            return when {
                averageByCourse.isEmpty() -> 0.0
                totalCredits <= 0.0 -> averageByCourse.values.average()
                else -> averageByCourse.entries.sumOf { (courseName, average) ->
                    average * (creditsByCourseName[courseName] ?: 0).toDouble()
                } / totalCredits
            }
        }
}

class GradesViewModel(container: AppContainer, studentId: String) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<GradesUiState> = kotlinx.coroutines.flow.combine(
        container.gradeRepository.observeCachedForStudent(studentId),
        container.courseRepository.observeCachedForStudent(studentId),
        _isRefreshing
    ) { grades, courses, refreshing -> GradesUiState(grades, courses, refreshing) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GradesUiState())

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            container.gradeRepository.refreshForStudent(studentId)
            container.courseRepository.refreshForStudent(studentId)
            _isRefreshing.value = false
        }
    }
}
