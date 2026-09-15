package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Notas reales ya publicadas por el profesor para un curso, una por corte (1, 2, 3) —
 * si el profesor publicó el mismo corte más de una vez (corrección), gana la más
 * reciente por [co.edu.eafit.appeafit.domain.model.Grade.date]. */
data class CourseGradeRow(
    val course: Course,
    val realCortes: Map<Int, Double>
)

data class GpaCalculatorUiState(
    val rows: List<CourseGradeRow> = emptyList(),
    val isRefreshing: Boolean = false
)

class GpaCalculatorViewModel(container: AppContainer, studentId: String) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<GpaCalculatorUiState> = combine(
        container.courseRepository.observeCachedForStudent(studentId),
        container.gradeRepository.observeCachedForStudent(studentId),
        _isRefreshing
    ) { courses, grades, refreshing ->
        val gradesByCourse = grades.groupBy { it.courseId }
        val rows = courses.map { course ->
            val realCortes = gradesByCourse[course.id]
                .orEmpty()
                .filter { it.corte in 1..3 }
                .groupBy { it.corte }
                .mapValues { (_, sameCorte) -> sameCorte.maxBy { it.date }.score }
            CourseGradeRow(course, realCortes)
        }
        GpaCalculatorUiState(rows, refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GpaCalculatorUiState())

    init {
        viewModelScope.launch {
            _isRefreshing.value = true
            container.courseRepository.refreshForStudent(studentId)
            container.gradeRepository.refreshForStudent(studentId)
            _isRefreshing.value = false
        }
    }
}
