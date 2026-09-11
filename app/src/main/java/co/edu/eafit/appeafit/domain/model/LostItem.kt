package co.edu.eafit.appeafit.domain.model

enum class LostItemStatus(val id: String, val label: String) {
    REPORTED("reported", "Reportado"),
    CLAIMED("claimed", "Reclamado")
}

data class LostItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val reportedBy: String = "",
    val status: String = LostItemStatus.REPORTED.id,
    val createdAt: Long = 0L
)
