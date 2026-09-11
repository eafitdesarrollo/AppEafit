package co.edu.eafit.appeafit.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val EVALUATIONS_COLLECTION = "teacherEvaluations"

class TeacherEvaluationRepository(private val firestore: FirebaseFirestore) {

    suspend fun submit(courseId: String, studentId: String, rating: Int, comment: String): Result<Unit> = runCatching {
        firestore.collection(EVALUATIONS_COLLECTION).add(
            mapOf(
                "courseId" to courseId,
                "studentId" to studentId,
                "rating" to rating,
                "comment" to comment,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Unit
    }

    suspend fun hasEvaluated(courseId: String, studentId: String): Boolean = runCatching {
        firestore.collection(EVALUATIONS_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("studentId", studentId)
            .get()
            .await()
            .documents
            .isNotEmpty()
    }.getOrDefault(false)
}
