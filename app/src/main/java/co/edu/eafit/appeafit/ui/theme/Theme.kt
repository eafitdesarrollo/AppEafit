package co.edu.eafit.appeafit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = EafitBlue,
    onPrimary = EafitWhite,
    primaryContainer = Color(0xFFD9E7FF),
    onPrimaryContainer = EafitBlueDark,
    secondary = EafitCoral,
    onSecondary = EafitWhite,
    secondaryContainer = Color(0xFFFFDBD0),
    onSecondaryContainer = EafitCoralDark,
    tertiary = EafitCyan,
    onTertiary = EafitWhite,
    tertiaryContainer = Color(0xFFCFF3FF),
    onTertiaryContainer = Color(0xFF004E66),
    background = BackgroundLight,
    onBackground = EafitBlack,
    surface = SurfaceLight,
    onSurface = EafitBlack,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighLight,
    error = StatusError,
    onError = EafitWhite,
    errorContainer = StatusErrorContainer,
    onErrorContainer = StatusError,
    outline = OutlineLight,
    outlineVariant = OutlineLight
)

private val DarkColors = darkColorScheme(
    primary = EafitBlueLight,
    onPrimary = EafitNavy,
    primaryContainer = EafitBlueDark,
    onPrimaryContainer = Color(0xFFD9E7FF),
    secondary = EafitCoral,
    onSecondary = EafitNavy,
    secondaryContainer = EafitCoralDark,
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = EafitCyan,
    onTertiary = EafitNavy,
    tertiaryContainer = Color(0xFF004E66),
    onTertiaryContainer = Color(0xFFCFF3FF),
    background = BackgroundDark,
    onBackground = EafitWhite,
    surface = SurfaceDark,
    onSurface = EafitWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLowest = BackgroundDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighDark,
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF3A0000),
    errorContainer = Color(0xFF7A1414),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = OutlineDark,
    outlineVariant = OutlineDark
)

val EafitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
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
        shapes = EafitShapes,
        content = content
    )
}
