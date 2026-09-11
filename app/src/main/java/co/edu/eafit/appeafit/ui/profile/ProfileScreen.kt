package co.edu.eafit.appeafit.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.BuildConfig
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.RoleBadge
import co.edu.eafit.appeafit.ui.navigation.Routes
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    user: User,
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user.photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(88.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    )
                } else {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(88.dp)) {
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(user.fullName.take(1).ifBlank { "E" }, style = MaterialTheme.typography.headlineLarge)
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                Text(user.fullName.ifBlank { user.email }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                if (user.program.isNotBlank()) {
                    Text(user.program, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                }
                RoleBadge(role = user.role)
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))

        ProfileMenuItem(Icons.Filled.Edit, stringResource(R.string.profile_edit_data)) {
            navController.navigate(Routes.EDIT_PROFILE)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        ProfileMenuItem(Icons.Filled.Lock, stringResource(R.string.profile_change_password)) {
            navController.navigate(Routes.CHANGE_PASSWORD)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        ProfileMenuItem(Icons.Filled.Settings, stringResource(R.string.profile_settings)) {
            navController.navigate(Routes.SETTINGS)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))
        ProfileMenuItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = stringResource(R.string.common_logout),
            highlighted = true,
            onClick = { showLogoutConfirm = true }
        )

        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))
        Text(
            text = stringResource(R.string.profile_app_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(stringResource(R.string.common_logout)) },
            text = { Text(stringResource(R.string.common_logout_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onSignOut()
                }) { Text(stringResource(R.string.common_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (highlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        tonalElevation = if (highlighted) 0.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = contentColor)
                androidx.compose.foundation.layout.Spacer(Modifier.size(14.dp))
                Text(label, color = contentColor, style = MaterialTheme.typography.titleSmall)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = contentColor)
        }
    }
}
