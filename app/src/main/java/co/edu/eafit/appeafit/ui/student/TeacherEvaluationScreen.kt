package co.edu.eafit.appeafit.ui.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.ui.components.EafitButton
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherEvaluationScreen(container: AppContainer, studentId: String, onBack: () -> Unit) {
    val viewModel: ScheduleViewModel = viewModel(factory = GenericViewModelFactory { ScheduleViewModel(container, studentId) })
    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var evaluatedCourseIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(courses) {
        evaluatedCourseIds = courses.filter { container.teacherEvaluationRepository.hasEvaluated(it.id, studentId) }
            .map { it.id }
            .toSet()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_teacher_evaluation)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (courses.isEmpty()) {
            EmptyState(message = "No tienes cursos para evaluar", icon = Icons.Filled.Star, modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(courses, key = { _, item -> item.id }) { index, course ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(250, delayMillis = index * 40)) +
                        slideInVertically(tween(250, delayMillis = index * 40)) { it / 5 }
                ) {
                    EvaluationCard(
                        course = course,
                        submitted = evaluatedCourseIds.contains(course.id),
                        onSubmit = { rating, comment ->
                            scope.launch {
                                container.teacherEvaluationRepository.submit(course.id, studentId, rating, comment)
                                evaluatedCourseIds = evaluatedCourseIds + course.id
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EvaluationCard(course: Course, submitted: Boolean, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    EafitCard {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                course.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                course.professorName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            if (submitted) {
                Text(
                    "¡Gracias por tu evaluación!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    for (i in 1..5) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 4.dp)
                                .clickable { rating = i }
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario (opcional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                EafitButton(
                    text = "Enviar evaluación",
                    onClick = { onSubmit(rating, comment) },
                    enabled = rating > 0,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
