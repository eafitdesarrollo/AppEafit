package co.edu.eafit.appeafit.domain.model

data class AttendanceRecord(
    val studentId: String = "",
    val studentName: String = "",
    val present: Boolean = true
)

data class AttendanceSession(
    val id: String = "",
    val courseId: String = "",
    val date: String = "",
    val records: List<AttendanceRecord> = emptyList()
)
