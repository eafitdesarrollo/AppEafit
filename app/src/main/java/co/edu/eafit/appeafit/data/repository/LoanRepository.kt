package co.edu.eafit.appeafit.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

private const val LOANS_COLLECTION = "loans"
private const val RENEWAL_DAYS = 7L

class LoanRepository(private val firestore: FirebaseFirestore) {

    suspend fun listForStudent(studentId: String) = runCatching {
        firestore.collection(LOANS_COLLECTION)
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("returned", false)
            .get()
            .await()
            .documents
            .map { it.toLoan() }
    }

    suspend fun renew(loanId: String, currentDueAt: Long): Result<Unit> = runCatching {
        val newDueAt = currentDueAt + TimeUnit.DAYS.toMillis(RENEWAL_DAYS)
        firestore.collection(LOANS_COLLECTION).document(loanId).update("dueAt", newDueAt).await()
        Unit
    }
}
