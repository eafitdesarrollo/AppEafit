package co.edu.eafit.appeafit.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.data.repository.LANGUAGE_SYSTEM
import co.edu.eafit.appeafit.data.repository.ThemeMode
import co.edu.eafit.appeafit.ui.components.EafitCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer, onBack: () -> Unit) {
    val notificationsEnabled by container.settingsRepository.notificationsEnabled.collectAsStateWithLifecycle(initialValue = true)
    val themeMode by container.settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
    val currentLanguage by container.settingsRepository.language.collectAsStateWithLifecycle(initialValue = LANGUAGE_SYSTEM)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_settings)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EafitCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            stringResource(R.string.settings_notifications_title),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            stringResource(R.string.settings_notifications_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            scope.launch { container.settingsRepository.setNotificationsEnabled(enabled) }
                        }
                    )
                }
            }

            EafitCard {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(stringResource(R.string.settings_appearance_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.settings_appearance_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            Triple(ThemeMode.SYSTEM, stringResource(R.string.settings_option_system), 0),
                            Triple(ThemeMode.LIGHT, stringResource(R.string.settings_theme_light), 1),
                            Triple(ThemeMode.DARK, stringResource(R.string.settings_theme_dark), 2)
                        )
                        options.forEach { (mode, label, index) ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { scope.launch { container.settingsRepository.setThemeMode(mode) } },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }
                }
            }

            EafitCard {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(stringResource(R.string.settings_language_title), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.settings_language_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 12.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            Triple(LANGUAGE_SYSTEM, stringResource(R.string.settings_option_system), 0),
                            Triple("es", "Español", 1),
                            Triple("en", "English", 2)
                        )
                        options.forEach { (tag, label, index) ->
                            SegmentedButton(
                                selected = currentLanguage == tag,
                                onClick = {
                                    if (currentLanguage == tag) return@SegmentedButton
                                    // El idioma se guarda en nuestro propio DataStore y se aplica en
                                    // MainActivity.attachBaseContext en el próximo arranque de la
                                    // Activity: depender solo de AppCompatDelegate en un
                                    // ComponentActivity no garantiza que el cambio se refleje de
                                    // inmediato (carrera con la propagación async del sistema).
                                    scope.launch {
                                        container.settingsRepository.setLanguage(tag)
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            )
                        }
                    }
                }
            }
        }
    }
}
