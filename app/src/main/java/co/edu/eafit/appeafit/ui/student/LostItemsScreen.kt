package co.edu.eafit.appeafit.ui.student

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import co.edu.eafit.appeafit.domain.model.LostItemStatus
import co.edu.eafit.appeafit.ui.components.EmptyState
import co.edu.eafit.appeafit.ui.components.ErrorState
import co.edu.eafit.appeafit.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostItemsScreen(
    container: AppContainer,
    reporterId: String,
    canManage: Boolean,
    onBack: () -> Unit
) {
    val viewModel: LostItemsViewModel = viewModel(factory = GenericViewModelFactory { LostItemsViewModel(container) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_lost_items)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        },
        floatingActionButton = {
            if (!canManage) {
                FloatingActionButton(onClick = { showReportDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Reportar objeto")
                }
            }
        }
    ) { padding ->
        when (val current = state) {
            is UiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorState(current.message, onRetry = viewModel::load, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (current.data.isEmpty()) {
                    EmptyState(message = "No hay objetos reportados", icon = Icons.Filled.Search, modifier = Modifier.padding(padding))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(current.data, key = { it.id }) { item ->
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
                                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            if (item.status == LostItemStatus.CLAIMED.id) "Reclamado" else "Reportado",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.status == LostItemStatus.CLAIMED.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                    }
                                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                                    Text(item.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (canManage && item.status != LostItemStatus.CLAIMED.id) {
                                        androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                                        Button(onClick = { viewModel.markClaimed(item.id) }) {
                                            Text("Marcar como reclamado")
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

    if (showReportDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Reportar objeto") },
            text = {
                Column {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Objeto") }, singleLine = true)
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") })
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lugar") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.report(title, description, location, reporterId)
                    showReportDialog = false
                }) { Text(stringResource(R.string.common_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
