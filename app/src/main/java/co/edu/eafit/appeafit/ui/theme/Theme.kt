package co.edu.eafit.appeafit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = EafitBlue,
    onPrimary = EafitWhite,
    primaryContainer = EafitBlue.copy(alpha = 0.12f),
    onPrimaryContainer = EafitBlueDark,
    secondary = EafitCyan,
    onSecondary = EafitWhite,
    background = EafitGrayBg,
    onBackground = EafitBlack,
    surface = EafitWhite,
    onSurface = EafitBlack,
    surfaceVariant = EafitGrayLine,
    onSurfaceVariant = EafitTextSecondary,
    error = StatusError,
    outline = EafitGrayLine
)

private val DarkColors = darkColorScheme(
    primary = EafitCyan,
    onPrimary = EafitBlack,
    primaryContainer = EafitBlueDark,
    onPrimaryContainer = EafitWhite,
    secondary = EafitCyan,
    onSecondary = EafitBlack,
    background = EafitNavy,
    onBackground = EafitWhite,
    surface = Color(0xFF141B33),
    onSurface = EafitWhite,
    surfaceVariant = Color(0xFF232A45),
    onSurfaceVariant = Color(0xFFC7CCDA),
    error = Color(0xFFFF6B6B),
    outline = Color(0xFF3A4160)
)

@Composable
fun AppEafitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EafitTypography,
        content = content
    )
}
