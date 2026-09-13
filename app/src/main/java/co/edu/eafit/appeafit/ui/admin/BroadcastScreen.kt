package co.edu.eafit.appeafit.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.ui.components.EafitButton
import co.edu.eafit.appeafit.ui.components.EafitCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastScreen(container: AppContainer, onBack: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("all") }
    var sent by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val targets = listOf("all" to "Todos") + Role.entries.map { it.id to it.label }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_broadcast)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
            OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Mensaje") }, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 16.dp))
            Text("Enviar a:", style = MaterialTheme.typography.titleSmall)
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 4.dp))
            EafitCard {
                Column {
                    targets.forEach { (id, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = target == id, onClick = { target = id })
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = target == id, onClick = { target = id })
                            Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 16.dp))
            EafitButton(
                text = if (sent) "Enviado ✓" else "Enviar notificación",
                onClick = {
                    scope.launch {
                        container.notificationRepository.broadcast(title, body, target)
                        sent = true
                        title = ""; body = ""
                    }
                },
                enabled = title.isNotBlank() && body.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
