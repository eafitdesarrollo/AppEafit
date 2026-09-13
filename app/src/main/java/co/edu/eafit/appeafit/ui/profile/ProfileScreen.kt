package co.edu.eafit.appeafit.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.edu.eafit.appeafit.BuildConfig
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.components.EafitCard
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
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 10 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            EafitCard(shape = MaterialTheme.shapes.extraLarge) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val avatarGradient = Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                    )
                    if (user.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(88.dp).background(avatarGradient, CircleShape)
                        )
                    } else {
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(88.dp).background(avatarGradient, CircleShape)
                        ) {
                            Text(
                                user.fullName.take(1).ifBlank { "E" },
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                    Text(
                        user.fullName.ifBlank { user.email },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.size(6.dp))
                    if (user.program.isNotBlank()) {
                        Text(
                            user.program,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
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
    EafitCard(
        onClick = onClick,
        color = containerColor,
        tonalElevation = if (highlighted) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(icon, contentDescription = null, tint = contentColor)
                androidx.compose.foundation.layout.Spacer(Modifier.size(14.dp))
                Text(
                    label,
                    color = contentColor,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = contentColor)
        }
    }
}
