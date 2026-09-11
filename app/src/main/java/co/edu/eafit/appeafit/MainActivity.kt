package co.edu.eafit.appeafit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import co.edu.eafit.appeafit.ui.navigation.EafitNavHost
import co.edu.eafit.appeafit.ui.theme.AppEafitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as EafitApplication).container

        setContent {
            AppEafitTheme {
                EafitNavHost(container = container)
            }
        }
    }
}
