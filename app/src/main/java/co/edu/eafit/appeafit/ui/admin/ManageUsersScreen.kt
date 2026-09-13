package co.edu.eafit.appeafit.ui.admin

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitCard
import co.edu.eafit.appeafit.ui.components.EmptyState
import co.edu.eafit.appeafit.ui.components.RoleBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(container: AppContainer, onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    // 2026-09-13: antes un admin podía quitarse (o quitarle a otro) el rol de admin, o
    // desactivar su cuenta, con un solo clic — si era el único admin activo, la app quedaba
    // sin ningún administrador y la única recuperación era manual desde la consola de
    // Firebase. Ahora, si la persona es la última cuenta admin activa, se pide confirmación
    // explícita antes de aplicar el cambio.
    var pendingRoleChange by remember { mutableStateOf<Pair<User, Role>?>(null) }
    var pendingDeactivate by remember { mutableStateOf<User?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch { container.userRepository.listUsers().onSuccess { users = it } }
    }

    fun isLastActiveAdmin(person: User): Boolean =
        person.role == Role.ADMIN && person.active &&
            users.count { it.role == Role.ADMIN && it.active } <= 1

    fun applyRoleChange(person: User, role: Role) {
        scope.launch { container.userRepository.updateRole(person.uid, role); reload() }
    }

    fun applyActiveChange(person: User, active: Boolean) {
        scope.launch { container.userRepository.setActive(person.uid, active); reload() }
    }

    LaunchedEffect(Unit) { reload() }

    val filtered = remember(users, query) {
        if (query.isBlank()) users
        else users.filter { it.fullName.contains(query, true) || it.email.contains(query, true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.service_manage_users)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.common_search_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            if (filtered.isEmpty()) {
                EmptyState(message = "No hay usuarios registrados", icon = Icons.Filled.SupervisedUserCircle, modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(filtered, key = { _, item -> item.uid }) { index, person ->
                        var roleMenuExpanded by remember { mutableStateOf(false) }
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(250, delayMillis = (index % 12) * 25)) +
                                slideInVertically(tween(250, delayMillis = (index % 12) * 25)) { it / 5 }
                        ) {
                            EafitCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                person.fullName.ifBlank { person.email },
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                person.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Switch(
                                            checked = person.active,
                                            onCheckedChange = { active ->
                                                if (!active && isLastActiveAdmin(person)) {
                                                    pendingDeactivate = person
                                                } else {
                                                    applyActiveChange(person, active)
                                                }
                                            }
                                        )
                                    }
                                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            onClick = { roleMenuExpanded = true }
                                        ) {
                                            RoleBadge(role = person.role, modifier = Modifier)
                                        }
                                        DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                                            Role.entries.forEach { role ->
                                                DropdownMenuItem(
                                                    text = { Text(role.label) },
                                                    onClick = {
                                                        roleMenuExpanded = false
                                                        if (role != Role.ADMIN && isLastActiveAdmin(person)) {
                                                            pendingRoleChange = person to role
                                                        } else {
                                                            applyRoleChange(person, role)
                                                        }
                                                    }
                                                )
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

    pendingRoleChange?.let { (person, role) ->
        AlertDialog(
            onDismissRequest = { pendingRoleChange = null },
            title = { Text("¿Quitar el último administrador?") },
            text = { Text("${person.fullName.ifBlank { person.email }} es el único administrador activo. Si le quitas el rol de admin, nadie más podrá gestionar usuarios ni contenido hasta que lo restaures manualmente desde la consola de Firebase. ¿Seguro que quieres continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    applyRoleChange(person, role)
                    pendingRoleChange = null
                }) { Text("Sí, quitar rol de admin") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRoleChange = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    pendingDeactivate?.let { person ->
        AlertDialog(
            onDismissRequest = { pendingDeactivate = null },
            title = { Text("¿Desactivar el último administrador?") },
            text = { Text("${person.fullName.ifBlank { person.email }} es el único administrador activo. Si lo desactivas, nadie más podrá gestionar usuarios ni contenido hasta que lo reactives manualmente desde la consola de Firebase. ¿Seguro que quieres continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    applyActiveChange(person, false)
                    pendingDeactivate = null
                }) { Text("Sí, desactivar") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeactivate = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
