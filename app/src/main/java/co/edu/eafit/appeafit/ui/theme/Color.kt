package co.edu.eafit.appeafit.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---- Marca EAFIT ----
val EafitBlue = Color(0xFF146AEF)
val EafitBlueDark = Color(0xFF0A3A8C)
val EafitBlueLight = Color(0xFF5B9BFF)
val EafitNavy = Color(0xFF060B1F)
val EafitCyan = Color(0xFF00C2FF)
val EafitBlack = Color(0xFF0A0A0A)
val EafitWhite = Color(0xFFFFFFFF)

// ---- Acento cálido: rompe el monocromatismo azul, se usa para CTAs
// secundarios, resaltados y estados de "atención" (no para textos de error) ----
val EafitCoral = Color(0xFFFF6B4A)
val EafitCoralDark = Color(0xFFD84E2F)
val EafitAmber = Color(0xFFFFB020)

// ---- Superficies (light) ----
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF3F5FA)
val SurfaceContainerLight = Color(0xFFEBEFF7)
val SurfaceContainerHighLight = Color(0xFFE1E7F3)
val BackgroundLight = Color(0xFFF6F7FB)
val OutlineLight = Color(0xFFDCE1EC)
val TextSecondaryLight = Color(0xFF5B6270)

// ---- Superficies (dark) ----
// OJO: en la primera versión estos tonos quedaban casi idénticos al fondo (todo se veía
// "un bloque negro plano" — el problema #1 reportado). Ahora hay un salto de luminancia
// grande y deliberado entre background y las superficies de tarjeta.
val BackgroundDark = Color(0xFF0A0D18)
val SurfaceContainerLowDark = Color(0xFF141936)
val SurfaceDark = Color(0xFF1D2545)
val SurfaceContainerDark = Color(0xFF1D2545)
val SurfaceContainerHighDark = Color(0xFF2A3363)
val OutlineDark = Color(0xFF454F82)
val TextSecondaryDark = Color(0xFFB7BFDD)

// ---- Acentos de rol (badges) ----
val RoleStudent = Color(0xFF146AEF)
val RoleProfessor = Color(0xFF0E8F6B)
val RoleStaff = Color(0xFFB5651D)
val RoleAdmin = Color(0xFF7A3BCF)

// ---- Estados semánticos ----
val StatusSuccess = Color(0xFF1E9E5A)
val StatusSuccessContainer = Color(0xFFD7F4E3)
val StatusWarning = Color(0xFFC77800)
val StatusWarningContainer = Color(0xFFFFEACC)
val StatusError = Color(0xFFBA1A1A)
val StatusErrorContainer = Color(0xFFFFDAD6)

// ---- Gradientes reutilizables para cabeceras "hero" (login, splash, carnet,
// encabezado de home) — el mismo look en todas partes en vez de cada pantalla
// inventando su propio Box(color = navy) ----
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
