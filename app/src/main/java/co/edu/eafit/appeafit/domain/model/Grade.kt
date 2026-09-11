package co.edu.eafit.appeafit.domain.model

data class Grade(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val item: String = "",
    val score: Double = 0.0,
    val maxScore: Double = 5.0,
    val weightPercent: Double = 0.0,
    val date: Long = 0L
)
