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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonRemove
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Enrollment
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch

/**
 * Gestión de matrículas (función de registraduría): elegir un curso y
 * matricular/desmatricular estudiantes -- antes no existía ninguna pantalla
 * para esto en la app, aunque `enrollments` ya era escribible por
 * staff/admin desde el origen del proyecto. Ver BITACORA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageEnrollmentsScreen(container: AppContainer, onBack: () -> Unit) {
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var courseMenuExpanded by remember { mutableStateOf(false) }
    var enrollments by remember { mutableStateOf<List<Enrollment>>(emptyList()) }
    var showEnrollDialog by remember { mutableStateOf(false) }
    var isLoadingEnrollments by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun reloadEnrollments() {
        val course = selectedCourse ?: return
        isLoadingEnrollments = true
        scope.launch {
            container.courseRepository.listEnrollmentsForCourse(course.id).onSuccess { enrollments = it }
            isLoadingEnrollments = false
        }
    }

    LaunchedEffect(Unit) {
        container.courseRepository.listAllCourses().onSuccess { courses = it }
        container.userRepository.listUsers().onSuccess { users -> students = users.filter { it.role == Role.STUDENT } }
    }

    LaunchedEffect(selectedCourse) { reloadEnrollments() }

    val studentById = remember(students) { students.associateBy { it.uid } }
    val enrolledStudentIds = remember(enrollments) { enrollments.map { it.studentId }.toSet() }
    val availableStudents = remember(students, enrolledStudentIds) {
        students.filter { it.uid !in enrolledStudentIds }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_enrollments)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            if (selectedCourse != null) {
                FloatingActionButton(onClick = { showEnrollDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_enrollments_add))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.padding(16.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = { courseMenuExpanded = true }
                ) {
                    Text(
                        selectedCourse?.let { "${it.name} (${it.code})" } ?: stringResource(R.string.manage_enrollments_pick_course),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
                DropdownMenu(expanded = courseMenuExpanded, onDismissRequest = { courseMenuExpanded = false }) {
                    if (courses.isEmpty()) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.manage_courses_empty)) }, onClick = {}, enabled = false)
                    }
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text("${course.name} (${course.code})") },
                            onClick = { selectedCourse = course; courseMenuExpanded = false }
                        )
                    }
                }
            }

            when {
                selectedCourse == null -> EmptyState(
                    message = stringResource(R.string.manage_enrollments_pick_course_hint),
                    icon = Icons.Filled.HowToReg,
                    modifier = Modifier.fillMaxSize()
                )
                isLoadingEnrollments -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                enrollments.isEmpty() -> EmptyState(
                    message = stringResource(R.string.manage_enrollments_empty),
                    icon = Icons.Filled.HowToReg,
                    modifier = Modifier.fillMaxSize()
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(enrollments, key = { _, item -> item.id }) { index, enrollment ->
                        val student = studentById[enrollment.studentId]
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
                                            student?.fullName?.ifBlank { student.email } ?: enrollment.studentId,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (student != null) {
                                            Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            container.courseRepository.unenroll(enrollment.id)
                                            reloadEnrollments()
                                        }
                                    }) {
                                        Icon(Icons.Filled.PersonRemove, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEnrollDialog && selectedCourse != null) {
        EnrollStudentDialog(
            availableStudents = availableStudents,
            onDismiss = { showEnrollDialog = false },
            onEnroll = { student ->
                scope.launch {
                    container.courseRepository.enrollStudent(student.uid, selectedCourse!!.id)
                    reloadEnrollments()
                    showEnrollDialog = false
                }
            }
        )
    }
}

@Composable
private fun EnrollStudentDialog(
    availableStudents: List<User>,
    onDismiss: () -> Unit,
    onEnroll: (User) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(availableStudents, query) {
        if (query.isBlank()) availableStudents
        else availableStudents.filter { it.fullName.contains(query, true) || it.email.contains(query, true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manage_enrollments_add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.common_search_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                if (filtered.isEmpty()) {
                    Text(stringResource(R.string.manage_enrollments_no_students_available), style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).padding(top = 4.dp)) {
                        itemsIndexed(filtered, key = { _, item -> item.uid }) { _, student ->
                            Surface(onClick = { onEnroll(student) }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                                    Text(student.fullName.ifBlank { student.email }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(student.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        }
    )
}
