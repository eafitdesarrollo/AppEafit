package co.edu.eafit.appeafit.ui.staff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.ContactMessage
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Buzón de mensajes del formulario de contacto del sitio web (PQRS
 * institucional) -- la colección `contact_messages` existe desde
 * 2026-09-17 y ya era legible/editable por staff/admin (comentario en las
 * reglas de Firestore decía explícitamente "para que Administrativos
 * puedan revisarlos"), pero nunca se construyó la pantalla. Ver BITACORA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactMessagesScreen(container: AppContainer, onBack: () -> Unit) {
    var messages by remember { mutableStateOf<List<ContactMessage>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("es-CO")) }

    fun reload() {
        scope.launch { container.contactMessageRepository.listAll().onSuccess { messages = it } }
    }

    LaunchedEffect(Unit) { reload() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_contact_messages)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        if (messages.isEmpty()) {
            EmptyState(message = stringResource(R.string.contact_messages_empty), icon = Icons.Filled.MailOutline, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(messages, key = { _, item -> item.id }) { index, message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(250, delayMillis = (index % 12) * 30)) +
                            slideInVertically(tween(250, delayMillis = (index % 12) * 30)) { it / 5 }
                    ) {
                        EafitCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(message.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(message.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (message.phone.isNotBlank()) {
                                            Text(message.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (message.resolved) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 4.dp))
                                                Text(stringResource(R.string.contact_messages_resolved), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                                Text(message.message, style = MaterialTheme.typography.bodyMedium)
                                androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        dateFormat.format(Date(message.createdAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            scope.launch {
                                                container.contactMessageRepository.setResolved(message.id, !message.resolved)
                                                reload()
                                            }
                                        }) {
                                            Icon(
                                                if (message.resolved) Icons.Filled.MarkEmailUnread else Icons.Filled.MarkEmailRead,
                                                contentDescription = null
                                            )
                                        }
                                        IconButton(onClick = {
                                            scope.launch {
                                                container.contactMessageRepository.delete(message.id)
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
    }
}
