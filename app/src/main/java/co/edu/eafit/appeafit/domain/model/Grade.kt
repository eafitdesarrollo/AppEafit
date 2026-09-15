package co.edu.eafit.appeafit.domain.model

/**
 * [corte] identifica a qué periodo de evaluación pertenece la nota: 1, 2 o 3 (el
 * profesor publica una nota por corte desde [co.edu.eafit.appeafit.ui.professor.GradeEntryScreen]).
 * `0` = nota sin corte asociado (compatibilidad con notas creadas antes de este campo,
 * que seguían el modelo libre de ítems). [item] se sigue guardando ("Corte 1", "Corte 2",
 * "Corte 3") para que [co.edu.eafit.appeafit.ui.student.GradesScreen] no necesite cambios.
 */
data class Grade(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val item: String = "",
    val score: Double = 0.0,
    val maxScore: Double = 5.0,
    val weightPercent: Double = 0.0,
    val date: Long = 0L,
    val corte: Int = 0
)
