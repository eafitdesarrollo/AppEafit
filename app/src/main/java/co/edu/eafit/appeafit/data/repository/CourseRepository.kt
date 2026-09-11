package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.CourseDao
import co.edu.eafit.appeafit.data.local.dao.EnrollmentDao
import co.edu.eafit.appeafit.data.local.entity.CachedCourseEntity
import co.edu.eafit.appeafit.data.local.entity.CachedEnrollmentEntity
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.ScheduleSlot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val COURSES_COLLECTION = "courses"
private const val ENROLLMENTS_COLLECTION = "enrollments"

@Serializable
private data class ScheduleSlotDto(val day: String, val startTime: String, val endTime: String, val room: String)

class CourseRepository(
    private val firestore: FirebaseFirestore,
    private val courseDao: CourseDao,
    private val enrollmentDao: EnrollmentDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    private fun CachedCourseEntity.toDomain(): Course {
        val slots = runCatching { json.decodeFromString<List<ScheduleSlotDto>>(scheduleJson) }.getOrDefault(emptyList())
        return Course(
            id = id,
            name = name,
            code = code,
            professorId = professorId,
            professorName = professorName,
            credits = credits,
            schedule = slots.map { ScheduleSlot(it.day, it.startTime, it.endTime, it.room) }
        )
    }

    private fun Course.toEntity(): CachedCourseEntity {
        val slots = schedule.map { ScheduleSlotDto(it.day, it.startTime, it.endTime, it.room) }
        return CachedCourseEntity(
            id = id,
            name = name,
            code = code,
            professorId = professorId,
            professorName = professorName,
            credits = credits,
            scheduleJson = json.encodeToString(slots)
        )
    }

    /** Fully reactive against Room: works offline as soon as one successful sync has happened. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun observeCachedForStudent(studentId: String): Flow<List<Course>> =
        enrollmentDao.observeCourseIds(studentId).flatMapLatest { ids ->
            if (ids.isEmpty()) flowOf(emptyList()) else courseDao.observeByIds(ids).map { list -> list.map { it.toDomain() } }
        }

    fun observeCachedForProfessor(professorId: String): Flow<List<Course>> =
        courseDao.observeByProfessor(professorId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshForStudent(studentId: String): Result<Unit> = runCatching {
        val enrollmentDocs = firestore.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
        val courseIds = enrollmentDocs.documents.mapNotNull { it.getString("courseId") }
        enrollmentDao.clearForStudent(studentId)
        enrollmentDao.upsertAll(courseIds.map { CachedEnrollmentEntity(studentId, it) })
        if (courseIds.isNotEmpty()) {
            val courses = firestore.collection(COURSES_COLLECTION)
                .whereIn("__name__", courseIds.take(30))
                .get()
                .await()
                .documents
                .map { it.toCourse() }
            courseDao.upsertAll(courses.map { it.toEntity() })
        }
    }

    suspend fun refreshForProfessor(professorId: String): Result<Unit> = runCatching {
        val courses = firestore.collection(COURSES_COLLECTION)
            .whereEqualTo("professorId", professorId)
            .get()
            .await()
            .documents
            .map { it.toCourse() }
        courseDao.upsertAll(courses.map { it.toEntity() })
    }

    suspend fun createCourse(course: Course): Result<Unit> = runCatching {
        firestore.collection(COURSES_COLLECTION).add(course.toMap()).await()
        Unit
    }

    suspend fun enrollStudent(studentId: String, courseId: String): Result<Unit> = runCatching {
        firestore.collection(ENROLLMENTS_COLLECTION).add(
            mapOf("studentId" to studentId, "courseId" to courseId)
        ).await()
        Unit
    }

    suspend fun listAllCourses(): Result<List<Course>> = runCatching {
        firestore.collection(COURSES_COLLECTION).get().await().documents.map { it.toCourse() }
    }

    suspend fun listEnrolledStudents(courseId: String): Result<List<co.edu.eafit.appeafit.domain.model.User>> = runCatching {
        val studentIds = firestore.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("studentId") }
        if (studentIds.isEmpty()) return@runCatching emptyList()
        firestore.collection("users")
            .whereIn("__name__", studentIds.take(30))
            .get()
            .await()
            .documents
            .map { it.toUser() }
    }
}
