package co.edu.eafit.appeafit.domain.model

enum class ReservationStatus(val id: String, val label: String) {
    PENDING("pending", "Pendiente"),
    APPROVED("approved", "Aprobada"),
    REJECTED("rejected", "Rechazada"),
    CANCELLED("cancelled", "Cancelada")
}

data class Space(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val capacity: Int = 0,
    val type: String = ""
)

data class Reservation(
    val id: String = "",
    val spaceId: String = "",
    val spaceName: String = "",
    val userId: String = "",
    val userName: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val purpose: String = "",
    val status: String = ReservationStatus.PENDING.id,
    val createdAt: Long = 0L
)
