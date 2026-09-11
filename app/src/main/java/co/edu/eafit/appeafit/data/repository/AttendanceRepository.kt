package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.AttendanceSession
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val ATTENDANCE_COLLECTION = "attendance"

class AttendanceRepository(private val firestore: FirebaseFirestore) {

    suspend fun submit(session: AttendanceSession): Result<Unit> = runCatching {
        firestore.collection(ATTENDANCE_COLLECTION).add(session.toMap()).await()
        Unit
    }

    suspend fun historyForCourse(courseId: String): Result<List<AttendanceSession>> = runCatching {
        firestore.collection(ATTENDANCE_COLLECTION)
            .whereEqualTo("courseId", courseId)
            .get()
            .await()
            .documents
            .map { it.toAttendanceSession() }
    }
}
