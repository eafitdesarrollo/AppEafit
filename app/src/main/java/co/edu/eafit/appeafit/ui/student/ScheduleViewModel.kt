package co.edu.eafit.appeafit.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Course
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(private val container: AppContainer, private val studentId: String) : ViewModel() {

    val courses: StateFlow<List<Course>> = container.courseRepository.observeCachedForStudent(studentId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { container.courseRepository.refreshForStudent(studentId) }
    }
}
