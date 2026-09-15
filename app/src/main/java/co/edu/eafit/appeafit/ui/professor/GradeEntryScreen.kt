package co.edu.eafit.appeafit.ui.professor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Grade
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitButton
import kotlinx.coroutines.launch

/** Peso igual para los 3 cortes (33.33% cada uno) — no se le pide al profesor porque
 * ninguna pantalla necesita mostrarlo hoy; ver GpaCalculatorViewModel/GradesViewModel,
 * que promedian por corte, no por peso libre, desde el pedido del 2026-09-15. */
private const val EQUAL_CUT_WEIGHT = 100.0 / 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEntryScreen(container: AppContainer, professorId: String, onBack: () -> Unit) {
    val coursesViewModel: ProfessorCoursesViewModel = viewModel(factory = GenericViewModelFactory { ProfessorCoursesViewModel(container, professorId) })
    val courses by coursesViewModel.courses.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var courseMenuExpanded by remember { mutableStateOf(false) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedStudent by remember { mutableStateOf<User?>(null) }
    var studentMenuExpanded by remember { mutableStateOf(false) }
    var selectedCut by remember { mutableIntStateOf(1) }
    var score by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    val cutLabels = listOf(
        stringResource(R.string.grade_entry_cut_1),
        stringResource(R.string.grade_entry_cut_2),
        stringResource(R.string.grade_entry_cut_3)
    )

    LaunchedEffect(selectedCourse) {
        val course = selectedCourse ?: return@LaunchedEffect
        container.courseRepository.listEnrolledStudents(course.id).onSuccess { students = it }
        selectedStudent = null
        saved = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_grade_entry)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Box {
                OutlinedButton(onClick = { courseMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedCourse?.name ?: stringResource(R.string.grade_entry_select_course), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = courseMenuExpanded, onDismissRequest = { courseMenuExpanded = false }) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { selectedCourse = course; courseMenuExpanded = false }
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
            Box {
                OutlinedButton(onClick = { studentMenuExpanded = true }, modifier = Modifier.fillMaxWidth(), enabled = students.isNotEmpty()) {
                    Text(selectedStudent?.fullName ?: stringResource(R.string.grade_entry_select_student), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                DropdownMenu(expanded = studentMenuExpanded, onDismissRequest = { studentMenuExpanded = false }) {
                    students.forEach { student ->
                        DropdownMenuItem(
                            text = { Text(student.fullName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = { selectedStudent = student; studentMenuExpanded = false }
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 20.dp))
            Text(stringResource(R.string.grade_entry_cut_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.grade_entry_cut_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                cutLabels.forEachIndexed { index, label ->
                    val cut = index + 1
                    SegmentedButton(
                        selected = selectedCut == cut,
                        onClick = { selectedCut = cut; saved = false },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = cutLabels.size),
                        label = { Text(label) }
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 20.dp))
            OutlinedTextField(
                value = score, onValueChange = { score = it; saved = false },
                label = { Text(stringResource(R.string.grade_entry_score)) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 20.dp))
            EafitButton(
                text = if (saved) stringResource(R.string.grade_entry_saved) else stringResource(R.string.grade_entry_save),
                onClick = {
                    val course = selectedCourse ?: return@EafitButton
                    val student = selectedStudent ?: return@EafitButton
                    scope.launch {
                        container.gradeRepository.addGrade(
                            Grade(
                                studentId = student.uid,
                                courseId = course.id,
                                courseName = course.name,
                                item = cutLabels[selectedCut - 1],
                                score = score.toDoubleOrNull() ?: 0.0,
                                maxScore = 5.0,
                                weightPercent = EQUAL_CUT_WEIGHT,
                                date = System.currentTimeMillis(),
                                corte = selectedCut
                            )
                        )
                        saved = true
                        score = ""
                    }
                },
                enabled = selectedCourse != null && selectedStudent != null && score.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
