package co.edu.eafit.appeafit.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: Role = Role.STUDENT,
    val program: String = "",
    val institutionalId: String = "",
    val photoUrl: String = "",
    val active: Boolean = true,
    val createdAt: Long = 0L
)
