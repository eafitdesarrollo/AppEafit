package co.edu.eafit.appeafit.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import co.edu.eafit.appeafit.core.util.UiState
import co.edu.eafit.appeafit.domain.model.ReservationStatus
import co.edu.eafit.appeafit.domain.model.Space
import co.edu.eafit.appeafit.ui.components.EmptyState
import co.edu.eafit.appeafit.ui.components.ErrorState
import co.edu.eafit.appeafit.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceReservationScreen(
    container: AppContainer,
    userId: String,
    userName: String,
    manageAll: Boolean,
    onBack: () -> Unit
) {
    val viewModel: ReservationViewModel = viewModel(factory = GenericViewModelFactory { ReservationViewModel(container, manageAll) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val spaces by viewModel.spaces.collectAsStateWithLifecycle()
    var showRequestDialog by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(Unit) { viewModel.load(userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_space_reservation)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            if (!manageAll) {
                FloatingActionButton(onClick = { showRequestDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Nueva reserva")
                }
            }
        }
    ) { padding ->
        when (val current = state) {
            is UiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(current.message, onRetry = { viewModel.load(userId) }, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    EmptyState(message = "No hay reservas todavía", icon = Icons.Filled.MeetingRoom, modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(current.data, key = { it.id }) { reservation ->
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(reservation.spaceName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            ReservationStatus.entries.firstOrNull { it.id == reservation.status }?.label ?: reservation.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (manageAll) Text(reservation.userName, style = MaterialTheme.typography.bodySmall)
                                    Text("${reservation.date} · ${reservation.startTime} - ${reservation.endTime}", style = MaterialTheme.typography.bodySmall)
                                    Text(reservation.purpose, style = MaterialTheme.typography.bodyMedium)
                                    if (manageAll && reservation.status == ReservationStatus.PENDING.id) {
                                        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(onClick = { viewModel.updateStatus(reservation.id, ReservationStatus.APPROVED.id, userId) }) {
                                                Text("Aprobar")
                                            }
                                            OutlinedButton(onClick = { viewModel.updateStatus(reservation.id, ReservationStatus.REJECTED.id, userId) }) {
                                                Text("Rechazar")
                                            }
                                        }
                                    }
                                    // Antes las reglas de Firestore ya permitían que el propio
                                    // usuario cancelara su reserva, pero ningún botón de la UI
                                    // lo exponía (solo staff podía aprobar/rechazar).
                                    if (!manageAll && reservation.userId == userId &&
                                        (reservation.status == ReservationStatus.PENDING.id || reservation.status == ReservationStatus.APPROVED.id)
                                    ) {
                                        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                                        OutlinedButton(onClick = { viewModel.cancelOwnReservation(reservation.id, userId) }) {
                                            Text("Cancelar reserva")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRequestDialog) {
        var selectedSpace by remember { mutableStateOf<Space?>(spaces.firstOrNull()) }
        var expanded by remember { mutableStateOf(false) }
        var date by remember { mutableStateOf("") }
        var start by remember { mutableStateOf("") }
        var end by remember { mutableStateOf("") }
        var purpose by remember { mutableStateOf("") }
        val createError by viewModel.createError.collectAsStateWithLifecycle()
        val successTick by viewModel.createSuccessTick.collectAsStateWithLifecycle()

        // El diálogo ya no se cierra apenas se pulsa "Guardar": si create() falla (p. ej.
        // por solapamiento de horario), antes el diálogo se cerraba igual y el usuario
        // nunca veía el motivo. Ahora solo se cierra cuando la reserva se crea con éxito.
        androidx.compose.runtime.LaunchedEffect(successTick) {
            if (successTick > 0) showRequestDialog = false
        }

        // Antes no había ninguna validación de formato: se podía "Guardar" con texto libre
        // en fecha/hora. Son reglas simples (no reemplazan un DatePicker/TimePicker real,
        // pendiente como mejora futura) pero evitan reservas con datos evidentemente inválidos.
        val dateValid = Regex("""\d{4}-\d{2}-\d{2}""").matches(date)
        val startValid = Regex("""\d{2}:\d{2}""").matches(start)
        val endValid = Regex("""\d{2}:\d{2}""").matches(end)
        val rangeValid = startValid && endValid && start < end
        val formValid = selectedSpace != null && dateValid && rangeValid && purpose.isNotBlank()

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Nueva reserva") },
            text = {
                Column {
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(selectedSpace?.name ?: "Selecciona un espacio")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            spaces.forEach { space ->
                                DropdownMenuItem(text = { Text(space.name) }, onClick = { selectedSpace = space; expanded = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = date, onValueChange = { date = it },
                        label = { Text("Fecha (aaaa-mm-dd)") }, singleLine = true,
                        isError = date.isNotEmpty() && !dateValid
                    )
                    OutlinedTextField(
                        value = start, onValueChange = { start = it },
                        label = { Text("Hora inicio (HH:mm)") }, singleLine = true,
                        isError = start.isNotEmpty() && !startValid
                    )
                    OutlinedTextField(
                        value = end, onValueChange = { end = it },
                        label = { Text("Hora fin (HH:mm)") }, singleLine = true,
                        isError = end.isNotEmpty() && (!endValid || (startValid && !rangeValid))
                    )
                    OutlinedTextField(value = purpose, onValueChange = { purpose = it }, label = { Text("Motivo") })
                    if (createError != null) {
                        Text(createError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = formValid,
                    onClick = {
                        selectedSpace?.let { space ->
                            viewModel.createReservation(userId, userName, space, date, start, end, purpose)
                        }
                    }
                ) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false; viewModel.clearCreateError() }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
