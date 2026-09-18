package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.CourseDao
import co.edu.eafit.appeafit.data.local.dao.EnrollmentDao
import co.edu.eafit.appeafit.data.local.entity.CachedCourseEntity
import co.edu.eafit.appeafit.data.local.entity.CachedEnrollmentEntity
import co.edu.eafit.appeafit.domain.model.Course
import co.edu.eafit.appeafit.domain.model.Enrollment
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
private data class ScheduleSlotDto(val day: String, val startTime: String, val endTime: String)

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
            schedule = slots.map { ScheduleSlot(it.day, it.startTime, it.endTime) }
        )
    }

    private fun Course.toEntity(): CachedCourseEntity {
        val slots = schedule.map { ScheduleSlotDto(it.day, it.startTime, it.endTime) }
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
            // Firestore limita whereIn a 30 valores por consulta: antes de esto, un
            // estudiante con más de 30 cursos matriculados simplemente dejaba de ver los
            // cursos 31 en adelante (se truncaba con .take(30) sin ningún aviso). Ahora se
            // consulta en bloques de 30 y se combinan los resultados.
            val courses = courseIds.chunked(30).flatMap { chunk ->
                firestore.collection(COURSES_COLLECTION)
                    .whereIn("__name__", chunk)
                    .get()
                    .await()
                    .documents
                    .map { it.toCourse() }
            }
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
        // Limpia antes de insertar: si no, un curso que el profesor deja de dictar
        // (documento borrado/reasignado) quedaba "fantasma" para siempre en el caché local.
        courseDao.clearForProfessor(professorId)
        courseDao.upsertAll(courses.map { it.toEntity() })
    }

    suspend fun createCourse(course: Course): Result<Unit> = runCatching {
        firestore.collection(COURSES_COLLECTION).add(course.toMap()).await()
        Unit
    }

    suspend fun updateCourse(course: Course): Result<Unit> = runCatching {
        firestore.collection(COURSES_COLLECTION).document(course.id).set(course.toMap()).await()
        Unit
    }

    /**
     * Borra el curso y, en cascada, todas sus matrículas -- si no, quedaban
     * matrículas "huérfanas" apuntando a un courseId que ya no existe, y
     * `refreshForStudent` seguía intentando resolverlas contra un curso
     * inexistente en cada sincronización.
     */
    suspend fun deleteCourse(courseId: String): Result<Unit> = runCatching {
        val enrollmentDocs = firestore.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
        enrollmentDocs.forEach { it.reference.delete().await() }
        firestore.collection(COURSES_COLLECTION).document(courseId).delete().await()
        Unit
    }

    suspend fun enrollStudent(studentId: String, courseId: String): Result<Unit> = runCatching {
        firestore.collection(ENROLLMENTS_COLLECTION).add(
            mapOf("studentId" to studentId, "courseId" to courseId)
        ).await()
        Unit
    }

    suspend fun unenroll(enrollmentId: String): Result<Unit> = runCatching {
        firestore.collection(ENROLLMENTS_COLLECTION).document(enrollmentId).delete().await()
        Unit
    }

    suspend fun listAllCourses(): Result<List<Course>> = runCatching {
        firestore.collection(COURSES_COLLECTION).get().await().documents.map { it.toCourse() }
    }

    suspend fun listEnrollmentsForCourse(courseId: String): Result<List<Enrollment>> = runCatching {
        firestore.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
            .map { doc ->
                Enrollment(
                    id = doc.id,
                    studentId = doc.getString("studentId").orEmpty(),
                    courseId = doc.getString("courseId").orEmpty()
                )
            }
    }

    suspend fun listEnrolledStudents(courseId: String): Result<List<co.edu.eafit.appeafit.domain.model.User>> = runCatching {
        val studentIds = firestore.collection(ENROLLMENTS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
            .mapNotNull { it.getString("studentId") }
        if (studentIds.isEmpty()) return@runCatching emptyList()
        // Mismo límite de whereIn(30) que arriba: se consulta en bloques para no truncar
        // silenciosamente la lista de estudiantes en cursos masivos.
        studentIds.chunked(30).flatMap { chunk ->
            firestore.collection("users")
                .whereIn("__name__", chunk)
                .get()
                .await()
                .documents
                .map { it.toUser() }
        }
    }
}
