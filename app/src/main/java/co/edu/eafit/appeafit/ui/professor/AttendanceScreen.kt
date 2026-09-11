package co.edu.eafit.appeafit.ui.professor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.AttendanceRecord
import co.edu.eafit.appeafit.domain.model.AttendanceSession
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(container: AppContainer, professorId: String, onBack: () -> Unit) {
    val coursesViewModel: ProfessorCoursesViewModel = viewModel(factory = GenericViewModelFactory { ProfessorCoursesViewModel(container, professorId) })
    val courses by coursesViewModel.courses.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf("") }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    val presentMap = remember { mutableStateMapOf<String, Boolean>() }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCourse) {
        val course = selectedCourse ?: return@LaunchedEffect
        container.courseRepository.listEnrolledStudents(course.id).onSuccess {
            students = it
            presentMap.clear()
            it.forEach { student -> presentMap[student.uid] = true }
        }
        submitted = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_attendance)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Box {
                OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedCourse?.name ?: "Selecciona un curso")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    courses.forEach { course ->
                        DropdownMenuItem(text = { Text(course.name) }, onClick = { selectedCourse = course; expanded = false })
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Fecha (aaaa-mm-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))

            if (selectedCourse == null) {
                EmptyState(message = "Selecciona un curso para tomar asistencia", modifier = Modifier.fillMaxSize())
            } else if (students.isEmpty()) {
                EmptyState(message = "Este curso no tiene estudiantes matriculados", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false).fillMaxWidth()) {
                    items(students, key = { it.uid }) { student ->
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(student.fullName.ifBlank { student.email })
                                Checkbox(
                                    checked = presentMap[student.uid] ?: true,
                                    onCheckedChange = { presentMap[student.uid] = it }
                                )
                            }
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                Button(
                    onClick = {
                        val course = selectedCourse ?: return@Button
                        scope.launch {
                            val records = students.map { AttendanceRecord(it.uid, it.fullName, presentMap[it.uid] ?: true) }
                            container.attendanceRepository.submit(AttendanceSession(courseId = course.id, date = date, records = records))
                            submitted = true
                        }
                    },
                    enabled = date.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (submitted) "Asistencia guardada ✓" else "Guardar asistencia")
                }
            }
        }
    }
}
