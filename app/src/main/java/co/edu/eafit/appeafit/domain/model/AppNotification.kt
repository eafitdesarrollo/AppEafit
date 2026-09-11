package co.edu.eafit.appeafit.domain.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val targetRole: String = "all",
    val targetUserId: String = "",
    val read: Boolean = false,
    val createdAt: Long = 0L
)
