package co.edu.eafit.appeafit.ui.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import co.edu.eafit.appeafit.domain.model.CalendarEvent
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestión del calendario académico institucional (crear, editar y borrar
 * eventos) -- antes el estudiante solo podía VER el calendario, y no existía
 * ninguna pantalla para que Administrativo lo mantenga actualizado, aunque
 * `calendarEvents` ya era escribible por staff/admin desde el origen del
 * proyecto. Ver BITACORA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCalendarScreen(container: AppContainer, onBack: () -> Unit) {
    var events by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CO")) }

    fun reload() {
        scope.launch { container.calendarRepository.refresh() }
    }

    LaunchedEffect(Unit) {
        reload()
        container.calendarRepository.observeCached().collect { events = it.sortedBy { e -> e.date } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_calendar)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingEvent = null; showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.manage_calendar_new))
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            EmptyState(message = stringResource(R.string.manage_calendar_empty), icon = Icons.Filled.EditCalendar, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(events, key = { _, item -> item.id }) { index, event ->
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
                                        event.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(dateFormat.format(Date(event.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    if (event.description.isNotBlank()) {
                                        Text(event.description, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { editingEvent = event; showDialog = true }) {
                                        Icon(Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(onClick = {
                                        scope.launch {
                                            container.calendarRepository.deleteEvent(event.id)
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
        CalendarEventEditorDialog(
            initial = editingEvent,
            onDismiss = { showDialog = false },
            onSave = { event ->
                scope.launch {
                    if (event.id.isBlank()) {
                        container.calendarRepository.createEvent(event)
                    } else {
                        container.calendarRepository.updateEvent(event)
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
private fun CalendarEventEditorDialog(
    initial: CalendarEvent?,
    onDismiss: () -> Unit,
    onSave: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var selectedDate by remember { mutableStateOf(initial?.date?.takeIf { it > 0L } ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-CO")) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(if (initial == null) R.string.manage_calendar_new else R.string.manage_calendar_edit)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text(stringResource(R.string.manage_calendar_title_label)) },
                    singleLine = true, enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text(stringResource(R.string.manage_calendar_description_label)) },
                    enabled = !isSaving, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = { if (!isSaving) showDatePicker = true }) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(dateFormat.format(Date(selectedDate)))
                }
            }
        },
        confirmButton = {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    enabled = title.isNotBlank(),
                    onClick = {
                        isSaving = true
                        onSave(
                            CalendarEvent(
                                id = initial?.id.orEmpty(),
                                title = title,
                                description = description,
                                date = selectedDate
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text(stringResource(R.string.common_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
