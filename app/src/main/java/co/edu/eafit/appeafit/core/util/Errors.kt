package co.edu.eafit.appeafit.core.util

/**
 * Traduce excepciones crudas de Firebase (Auth/Firestore) a mensajes en español
 * que se le pueden mostrar directamente al usuario. Compartido entre AuthViewModel
 * y ProfileViewModel para no duplicar (ni desincronizar) esta lógica.
 */
fun Throwable.toFriendlyAuthMessage(): String = when {
    message?.contains("password is invalid", ignoreCase = true) == true -> "Contraseña incorrecta"
    message?.contains("no user record", ignoreCase = true) == true -> "No existe una cuenta con ese correo"
    message?.contains("email address is already in use", ignoreCase = true) == true -> "Ya existe una cuenta con ese correo"
    message?.contains("requires recent authentication", ignoreCase = true) == true ||
        message?.contains("recent login", ignoreCase = true) == true ->
        "Por seguridad, vuelve a iniciar sesión e intenta de nuevo"
    message?.contains("network", ignoreCase = true) == true -> "Revisa tu conexión a internet"
    else -> message ?: "Ocurrió un error inesperado"
}
