package co.edu.eafit.appeafit.domain.model

/**
 * Mensaje enviado desde el formulario de contacto del sitio web
 * (appeafit-web-backend, colección compartida `contact_messages`). El campo
 * `resolved` no existe en los documentos que crea el backend (siempre nace
 * en false) -- lo gestiona exclusivamente esta app para que Administrativo
 * marque cuáles ya atendió.
 */
data class ContactMessage(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
    val createdAt: Long = 0L,
    val resolved: Boolean = false
)
