package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.GradeDao
import co.edu.eafit.appeafit.data.local.entity.CachedGradeEntity
import co.edu.eafit.appeafit.domain.model.Grade
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val GRADES_COLLECTION = "grades"

class GradeRepository(
    private val firestore: FirebaseFirestore,
    private val gradeDao: GradeDao
) {
    private fun CachedGradeEntity.toDomain() =
        Grade(id, studentId, courseId, courseName, item, score, maxScore, weightPercent, date, corte)

    private fun Grade.toEntity() =
        CachedGradeEntity(id, studentId, courseId, courseName, item, score, maxScore, weightPercent, date, corte)

    fun observeCachedForStudent(studentId: String): Flow<List<Grade>> =
        gradeDao.observeForStudent(studentId).map { list -> list.map { it.toDomain() } }

    suspend fun refreshForStudent(studentId: String): Result<Unit> = runCatching {
        val grades = firestore.collection(GRADES_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .map { it.toGrade() }
        gradeDao.clearForStudent(studentId)
        gradeDao.upsertAll(grades.map { it.toEntity() })
    }

    suspend fun addGrade(grade: Grade): Result<Unit> = runCatching {
        firestore.collection(GRADES_COLLECTION).add(grade.toMap()).await()
        Unit
    }

    suspend fun gradesForCourse(courseId: String): Result<List<Grade>> = runCatching {
        firestore.collection(GRADES_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
            .map { it.toGrade() }
    }
}
