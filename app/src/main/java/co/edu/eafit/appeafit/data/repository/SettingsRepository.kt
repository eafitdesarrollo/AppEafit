package co.edu.eafit.appeafit.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "eafit_settings")
private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
private val LANGUAGE_KEY = stringPreferencesKey("language_tag")

/** SYSTEM sigue el modo claro/oscuro del sistema operativo (comportamiento por
 * defecto); LIGHT/DARK lo fuerzan sin importar lo que diga el sistema. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Etiqueta de idioma persistida: "system", "es" o "en". Se lee de forma síncrona
 * en [co.edu.eafit.appeafit.MainActivity.attachBaseContext] para envolver el
 * Context con el Locale correcto antes de que se infle cualquier recurso, ya
 * que depender solo de AppCompatDelegate.setApplicationLocales() no garantiza
 * que el cambio de idioma se refleje de inmediato en un ComponentActivity. */
const val LANGUAGE_SYSTEM = "system"

class SettingsRepository(private val context: Context) {

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED_KEY] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { raw -> runCatching { ThemeMode.valueOf(raw) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE_KEY] ?: LANGUAGE_SYSTEM }

    suspend fun setLanguage(tag: String) {
        context.dataStore.edit { it[LANGUAGE_KEY] = tag }
    }

    /** Lectura bloqueante para usar en attachBaseContext, donde no hay forma de
     * esperar un Flow suspendido antes de inflar recursos. */
    fun languageBlocking(): String = kotlinx.coroutines.runBlocking {
        context.dataStore.data.map { it[LANGUAGE_KEY] ?: LANGUAGE_SYSTEM }.first()
    }
}
