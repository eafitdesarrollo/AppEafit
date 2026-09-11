package co.edu.eafit.appeafit.domain.model

enum class Role(val id: String, val label: String) {
    STUDENT("student", "Estudiante"),
    PROFESSOR("professor", "Profesor"),
    STAFF("staff", "Administrativo"),
    ADMIN("admin", "Administrador");

    companion object {
        fun fromId(id: String?): Role = entries.firstOrNull { it.id == id } ?: STUDENT
    }
}
