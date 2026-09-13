package co.edu.eafit.appeafit.domain.model

data class ScheduleSlot(
    val day: String = "",
    val startTime: String = "",
    val endTime: String = ""
)

data class Course(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val professorId: String = "",
    val professorName: String = "",
    val credits: Int = 0,
    val schedule: List<ScheduleSlot> = emptyList()
)

data class Enrollment(
    val id: String = "",
    val studentId: String = "",
    val courseId: String = ""
)
