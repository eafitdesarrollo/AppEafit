package co.edu.eafit.appeafit.ui.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.ScheduleSlot
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch

private val DAYS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

/**
 * Gestión de cursos para Administrativo/Registraduría (crear, editar y
 * borrar cursos, incluyendo su profesor y horario) -- antes esta función de
 * registraduría no tenía ninguna pantalla en la app, aunque las reglas de
 * Firestore ya permitían a staff/admin escribir en `courses` desde el
 * origen del proyecto. Ver BITACORA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCoursesScreen(container: AppContainer, onBack: () -> Unit) {
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var professors by remember { mutableStateOf<List<User>>(emptyList()) }
    var editingCourse by remember { mutableStateOf<Course?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch { container.courseRepository.listAllCourses().onSuccess { courses = it } }
        scope.launch {
            container.userRepository.listUsers().onSuccess { users ->
                professors = users.filter { it.role == Role.PROFESSOR }
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_courses)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingCourse = null; showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_courses_new))
            }
        }
    ) { padding ->
        if (courses.isEmpty()) {
            EmptyState(message = stringResource(R.string.manage_courses_empty), icon = Icons.Filled.School, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(courses, key = { _, item -> item.id }) { index, course ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250, delayMillis = (index % 12) * 30)) +
                            slideInVertically(tween(250, delayMillis = (index % 12) * 30)) { it / 5 }
                    ) {
                        EafitCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        course.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${course.code} · ${stringResource(R.string.course_credits, course.credits)}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        course.professorName.ifBlank { stringResource(R.string.manage_courses_no_professor) },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    course.schedule.forEach { slot ->
                                        Text("${slot.day} ${slot.startTime}-${slot.endTime}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { editingCourse = course; showDialog = true }) {
                                        Icon(Icons.Filled.Edit, contentDescription = null)
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            container.courseRepository.deleteCourse(course.id)
                                            reload()
                                        }
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CourseEditorDialog(
            initial = editingCourse,
            professors = professors,
            onDismiss = { showDialog = false },
            onSave = { course ->
                scope.launch {
                    if (course.id.isBlank()) {
                        container.courseRepository.createCourse(course)
                    } else {
                        container.courseRepository.updateCourse(course)
                    }
                    reload()
                    showDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseEditorDialog(
    initial: Course?,
    professors: List<User>,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var code by remember { mutableStateOf(initial?.code.orEmpty()) }
    var credits by remember { mutableStateOf(initial?.credits?.toString().orEmpty()) }
    var selectedProfessor by remember {
        mutableStateOf(professors.firstOrNull { it.uid == initial?.professorId })
    }
    var professorMenuExpanded by remember { mutableStateOf(false) }
    var schedule by remember { mutableStateOf(initial?.schedule ?: emptyList()) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(if (initial == null) R.string.manage_courses_new else R.string.manage_courses_edit)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.manage_courses_name)) },
                    singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = code, onValueChange = { code = it },
                    label = { Text(stringResource(R.string.manage_courses_code)) },
                    singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = credits, onValueChange = { credits = it.filter(Char::isDigit) },
                    label = { Text(stringResource(R.string.manage_courses_credits)) },
                    singleLine = true, enabled = !isSaving,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.manage_courses_professor), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = { if (!isSaving) professorMenuExpanded = true }
                    ) {
                        Text(
                            selectedProfessor?.fullName?.ifBlank { selectedProfessor?.email }
                                ?: stringResource(R.string.manage_courses_no_professor),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                    DropdownMenu(expanded = professorMenuExpanded, onDismissRequest = { professorMenuExpanded = false }) {
                        if (professors.isEmpty()) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.manage_courses_no_professors_available)) }, onClick = {}, enabled = false)
                        }
                        professors.forEach { professor ->
                            DropdownMenuItem(
                                text = { Text(professor.fullName.ifBlank { professor.email }) },
                                onClick = { selectedProfessor = professor; professorMenuExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.size(12.dp))
                Text(stringResource(R.string.manage_courses_schedule), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                schedule.forEachIndexed { index, slot ->
                    ScheduleSlotRow(
                        slot = slot,
                        enabled = !isSaving,
                        onChange = { updated -> schedule = schedule.toMutableList().also { it[index] = updated } },
                        onRemove = { schedule = schedule.toMutableList().also { it.removeAt(index) } }
                    )
                }
                TextButton(onClick = { schedule = schedule + ScheduleSlot(DAYS.first(), "", "") }, enabled = !isSaving) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(stringResource(R.string.manage_courses_add_slot))
                }
            }
        },
        confirmButton = {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    enabled = name.isNotBlank() && code.isNotBlank(),
                    onClick = {
                        isSaving = true
                        onSave(
                            Course(
                                id = initial?.id.orEmpty(),
                                name = name,
                                code = code,
                                professorId = selectedProfessor?.uid.orEmpty(),
                                professorName = selectedProfessor?.fullName.orEmpty(),
                                credits = credits.toIntOrNull() ?: 0,
                                schedule = schedule.filter { it.startTime.isNotBlank() && it.endTime.isNotBlank() }
                            )
                        )
                    }
                ) { Text(stringResource(R.string.common_save)) }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isSaving) onDismiss() }) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleSlotRow(
    slot: ScheduleSlot,
    enabled: Boolean,
    onChange: (ScheduleSlot) -> Unit,
    onRemove: () -> Unit
) {
    var dayMenuExpanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box {
            TextButton(onClick = { if (enabled) dayMenuExpanded = true }) { Text(slot.day) }
            DropdownMenu(expanded = dayMenuExpanded, onDismissRequest = { dayMenuExpanded = false }) {
                DAYS.forEach { day ->
                    DropdownMenuItem(text = { Text(day) }, onClick = { onChange(slot.copy(day = day)); dayMenuExpanded = false })
                }
            }
        }
        OutlinedTextField(
            value = slot.startTime, onValueChange = { onChange(slot.copy(startTime = it)) },
            placeholder = { Text("08:00") }, singleLine = true, enabled = enabled,
            modifier = Modifier.width(90.dp)
        )
        Text("–", modifier = Modifier.padding(horizontal = 4.dp))
        OutlinedTextField(
            value = slot.endTime, onValueChange = { onChange(slot.copy(endTime = it)) },
            placeholder = { Text("10:00") }, singleLine = true, enabled = enabled,
            modifier = Modifier.width(90.dp)
        )
        IconButton(onClick = onRemove, enabled = enabled) { Icon(Icons.Filled.Close, contentDescription = null) }
    }
}
