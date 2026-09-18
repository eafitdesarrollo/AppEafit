package co.edu.eafit.appeafit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---- Marca EAFIT (Actualizada) ----
val EafitBlue = Color(0xFF002855)       // Nuevo Azul Marino
val EafitBlueDark = Color(0xFF001533)   // Versión más oscura para el degradado
val EafitBlueLight = Color(0xFF1A457B)  // Versión más clara
val EafitNavy = Color(0xFF111111)       // Nuevo Gris Oscuro (Base)
val EafitCyan = Color(0xFF009A44)       // Nuevo Verde para acentos
val EafitBlack = Color(0xFF000000)      // Negro puro
val EafitWhite = Color(0xFFFFFFFF)      // Blanco puro

// ---- Acento cálido (Actualizado a nueva paleta) ----
val EafitCoral = Color(0xFFB85C38)      // Nuevo Naranja/Marrón
val EafitCoralDark = Color(0xFF8F4528)  // Naranja oscurecido
val EafitAmber = Color(0xFF2D6A4F)      // Nuevo Verde Bosque (Forest Green)

// ---- Superficies (light) ----
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF3F5FA)
val SurfaceContainerLight = Color(0xFFEBEFF7)
val SurfaceContainerHighLight = Color(0xFFE1E7F3)
val BackgroundLight = Color(0xFFF6F7FB)
val OutlineLight = Color(0xFFDCE1EC)
val TextSecondaryLight = Color(0xFF5B6270)

// ---- Superficies (dark) ----
// Mantenemos el salto de luminancia reportado, pero ahora basado en el nuevo #111111
val BackgroundDark = Color(0xFF111111)  // Nuevo fondo base
val SurfaceContainerLowDark = Color(0xFF1A1A1A)
val SurfaceDark = Color(0xFF222222)     // Tarjetas ligeramente más claras para resaltar
val SurfaceContainerDark = Color(0xFF222222)
val SurfaceContainerHighDark = Color(0xFF2D2D2D)
val OutlineDark = Color(0xFF444444)
val TextSecondaryDark = Color(0xFFB7BFDD)

// ---- Acentos de rol (badges) ----
val RoleStudent = Color(0xFF002855)     // Azul Marino
val RoleProfessor = Color(0xFF2D6A4F)   // Verde Bosque
val RoleStaff = Color(0xFFB85C38)       // Naranja/Marrón
val RoleAdmin = Color(0xFF7A3BCF)       // Mantenido para diferenciación

// ---- Estados semánticos ----
val StatusSuccess = Color(0xFF009A44)   // Usando el nuevo verde
val StatusSuccessContainer = Color(0xFFD7F4E3)
val StatusWarning = Color(0xFFC77800)
val StatusWarningContainer = Color(0xFFFFEACC)
val StatusError = Color(0xFFBA1A1A)
val StatusErrorContainer = Color(0xFFFFDAD6)

// ---- Gradientes reutilizables ----
val GradientHero = Brush.linearGradient(
    colors = listOf(EafitNavy, EafitBlueDark, EafitBlue)
)

val GradientHeroVertical = Brush.verticalGradient(
    colors = listOf(EafitNavy, EafitBlueDark, EafitBlue)
)

val GradientAccent = Brush.linearGradient(
    colors = listOf(EafitCoral, EafitAmber)
)

fun cardShimmerBrush(progress: Float, base: Color, highlight: Color): Brush {
    val width = 400f
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = androidx.compose.ui.geometry.Offset(progress * width - width, 0f),
        end = androidx.compose.ui.geometry.Offset(progress * width, width)
    )
}