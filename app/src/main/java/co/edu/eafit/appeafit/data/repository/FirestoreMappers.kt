package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.AppNotification
import co.edu.eafit.appeafit.domain.model.AttendanceRecord
import co.edu.eafit.appeafit.domain.model.AttendanceSession
import co.edu.eafit.appeafit.domain.model.CalendarEvent
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Grade
import co.edu.eafit.appeafit.domain.model.Loan
import co.edu.eafit.appeafit.domain.model.LostItem
import co.edu.eafit.appeafit.domain.model.NewsItem
import co.edu.eafit.appeafit.domain.model.Reservation
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.ScheduleSlot
import co.edu.eafit.appeafit.domain.model.Space
import co.edu.eafit.appeafit.domain.model.User
import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.toUser(): User = User(
    uid = id,
    email = getString("email").orEmpty(),
    fullName = getString("fullName").orEmpty(),
    role = Role.fromId(getString("role")),
    program = getString("program").orEmpty(),
    institutionalId = getString("institutionalId").orEmpty(),
    photoUrl = getString("photoUrl").orEmpty(),
    active = getBoolean("active") ?: true,
    createdAt = getLong("createdAt") ?: 0L
)

fun User.toMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "fullName" to fullName,
    "role" to role.id,
    "program" to program,
    "institutionalId" to institutionalId,
    "photoUrl" to photoUrl,
    "active" to active,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toNewsItem(): NewsItem = NewsItem(
    id = id,
    title = getString("title").orEmpty(),
    category = getString("category").orEmpty(),
    body = getString("body").orEmpty(),
    imageUrl = getString("imageUrl").orEmpty(),
    authorId = getString("authorId").orEmpty(),
    publishedAt = getLong("publishedAt") ?: 0L
)

fun NewsItem.toMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "category" to category,
    "body" to body,
    "imageUrl" to imageUrl,
    "authorId" to authorId,
    "publishedAt" to publishedAt
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toCourse(): Course {
    val scheduleRaw = get("schedule") as? List<Map<String, Any?>> ?: emptyList()
    val schedule = scheduleRaw.map {
        ScheduleSlot(
            day = it["day"] as? String ?: "",
            startTime = it["startTime"] as? String ?: "",
            endTime = it["endTime"] as? String ?: "",
            room = it["room"] as? String ?: ""
        )
    }
    return Course(
        id = id,
        name = getString("name").orEmpty(),
        code = getString("code").orEmpty(),
        professorId = getString("professorId").orEmpty(),
        professorName = getString("professorName").orEmpty(),
        credits = (getLong("credits") ?: 0L).toInt(),
        schedule = schedule
    )
}

fun Course.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "code" to code,
    "professorId" to professorId,
    "professorName" to professorName,
    "credits" to credits,
    "schedule" to schedule.map {
        mapOf("day" to it.day, "startTime" to it.startTime, "endTime" to it.endTime, "room" to it.room)
    }
)

fun DocumentSnapshot.toGrade(): Grade = Grade(
    id = id,
    studentId = getString("studentId").orEmpty(),
    courseId = getString("courseId").orEmpty(),
    courseName = getString("courseName").orEmpty(),
    item = getString("item").orEmpty(),
    score = getDouble("score") ?: 0.0,
    maxScore = getDouble("maxScore") ?: 5.0,
    weightPercent = getDouble("weightPercent") ?: 0.0,
    date = getLong("date") ?: 0L
)

fun Grade.toMap(): Map<String, Any?> = mapOf(
    "studentId" to studentId,
    "courseId" to courseId,
    "courseName" to courseName,
    "item" to item,
    "score" to score,
    "maxScore" to maxScore,
    "weightPercent" to weightPercent,
    "date" to date
)

fun DocumentSnapshot.toLostItem(): LostItem = LostItem(
    id = id,
    title = getString("title").orEmpty(),
    description = getString("description").orEmpty(),
    location = getString("location").orEmpty(),
    imageUrl = getString("imageUrl").orEmpty(),
    reportedBy = getString("reportedBy").orEmpty(),
    status = getString("status").orEmpty().ifEmpty { "reported" },
    createdAt = getLong("createdAt") ?: 0L
)

fun LostItem.toMap(): Map<String, Any?> = mapOf(
    "title" to title,
    "description" to description,
    "location" to location,
    "imageUrl" to imageUrl,
    "reportedBy" to reportedBy,
    "status" to status,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toLoan(): Loan = Loan(
    id = id,
    studentId = getString("studentId").orEmpty(),
    itemTitle = getString("itemTitle").orEmpty(),
    loanedAt = getLong("loanedAt") ?: 0L,
    dueAt = getLong("dueAt") ?: 0L,
    returned = getBoolean("returned") ?: false
)

fun DocumentSnapshot.toSpace(): Space = Space(
    id = id,
    name = getString("name").orEmpty(),
    location = getString("location").orEmpty(),
    capacity = (getLong("capacity") ?: 0L).toInt(),
    type = getString("type").orEmpty()
)

fun DocumentSnapshot.toReservation(): Reservation = Reservation(
    id = id,
    spaceId = getString("spaceId").orEmpty(),
    spaceName = getString("spaceName").orEmpty(),
    userId = getString("userId").orEmpty(),
    userName = getString("userName").orEmpty(),
    date = getString("date").orEmpty(),
    startTime = getString("startTime").orEmpty(),
    endTime = getString("endTime").orEmpty(),
    purpose = getString("purpose").orEmpty(),
    status = getString("status").orEmpty().ifEmpty { "pending" },
    createdAt = getLong("createdAt") ?: 0L
)

fun Reservation.toMap(): Map<String, Any?> = mapOf(
    "spaceId" to spaceId,
    "spaceName" to spaceName,
    "userId" to userId,
    "userName" to userName,
    "date" to date,
    "startTime" to startTime,
    "endTime" to endTime,
    "purpose" to purpose,
    "status" to status,
    "createdAt" to createdAt
)

fun DocumentSnapshot.toAppNotification(): AppNotification = AppNotification(
    id = id,
    title = getString("title").orEmpty(),
    body = getString("body").orEmpty(),
    targetRole = getString("targetRole").orEmpty().ifEmpty { "all" },
    targetUserId = getString("targetUserId").orEmpty(),
    read = getBoolean("read") ?: false,
    createdAt = getLong("createdAt") ?: 0L
)

fun DocumentSnapshot.toCalendarEvent(): CalendarEvent = CalendarEvent(
    id = id,
    title = getString("title").orEmpty(),
    description = getString("description").orEmpty(),
    date = getLong("date") ?: 0L
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toAttendanceSession(): AttendanceSession {
    val recordsRaw = get("records") as? List<Map<String, Any?>> ?: emptyList()
    val records = recordsRaw.map {
        AttendanceRecord(
            studentId = it["studentId"] as? String ?: "",
            studentName = it["studentName"] as? String ?: "",
            present = it["present"] as? Boolean ?: true
        )
    }
    return AttendanceSession(
        id = id,
        courseId = getString("courseId").orEmpty(),
        date = getString("date").orEmpty(),
        records = records
    )
}

fun AttendanceSession.toMap(): Map<String, Any?> = mapOf(
    "courseId" to courseId,
    "date" to date,
    "records" to records.map {
        mapOf("studentId" to it.studentId, "studentName" to it.studentName, "present" to it.present)
    }
)
