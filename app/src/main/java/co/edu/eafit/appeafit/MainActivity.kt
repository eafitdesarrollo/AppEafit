package co.edu.eafit.appeafit

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.eafit.appeafit.data.repository.LANGUAGE_SYSTEM
import co.edu.eafit.appeafit.data.repository.SettingsRepository
import co.edu.eafit.appeafit.data.repository.ThemeMode
import co.edu.eafit.appeafit.ui.navigation.EafitNavHost
import co.edu.eafit.appeafit.ui.theme.AppEafitTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageTag = SettingsRepository(newBase).languageBlocking()
        if (languageTag == LANGUAGE_SYSTEM) {
            super.attachBaseContext(newBase)
            return
        }
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as EafitApplication).container

        setContent {
            val themeMode by container.settingsRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val isDark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            AppEafitTheme(darkTheme = isDark) {
                EafitNavHost(container = container)
            }
        }
    }
}
