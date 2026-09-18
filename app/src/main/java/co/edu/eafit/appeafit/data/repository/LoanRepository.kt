package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.Loan
import co.edu.eafit.appeafit.domain.model.MAX_LOAN_RENEWALS
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    /** Todos los préstamos, para el panel de Administrativo (no solo los de un estudiante). */
    suspend fun listAll(): Result<List<Loan>> = runCatching {
        firestore.collection(LOANS_COLLECTION)
            .orderBy("loanedAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toLoan() }
    }

    suspend fun create(loan: Loan): Result<Unit> = runCatching {
        firestore.collection(LOANS_COLLECTION).add(loan.toMap()).await()
        Unit
    }

    suspend fun markReturned(loanId: String): Result<Unit> = runCatching {
        firestore.collection(LOANS_COLLECTION).document(loanId).update("returned", true).await()
        Unit
    }

    suspend fun delete(loanId: String): Result<Unit> = runCatching {
        firestore.collection(LOANS_COLLECTION).document(loanId).delete().await()
        Unit
    }

    /**
     * Antes: `dueAt` nuevo se calculaba a partir del `currentDueAt` recibido como parámetro
     * (capturado en la UI antes de la respuesta), sin límite de renovaciones ni verificar
     * que el préstamo siguiera sin devolver — un doble tap disparaba dos escrituras y no
     * había tope. Ahora se usa una transacción que lee el estado real del préstamo en el
     * servidor, valida que no esté devuelto ni supere MAX_LOAN_RENEWALS, y solo entonces
     * incrementa `dueAt` y `renewalCount` de forma atómica.
     */
    suspend fun renew(loanId: String): Result<Unit> = runCatching {
        val ref = firestore.collection(LOANS_COLLECTION).document(loanId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val returned = snapshot.getBoolean("returned") ?: false
            val renewalCount = (snapshot.getLong("renewalCount") ?: 0L).toInt()
            if (returned) error("Este préstamo ya fue devuelto")
            if (renewalCount >= MAX_LOAN_RENEWALS) error("Ya alcanzaste el máximo de $MAX_LOAN_RENEWALS renovaciones")
            val currentDueAt = snapshot.getLong("dueAt") ?: 0L
            val newDueAt = currentDueAt + TimeUnit.DAYS.toMillis(RENEWAL_DAYS)
            transaction.update(ref, mapOf("dueAt" to newDueAt, "renewalCount" to renewalCount + 1))
            null
        }.await()
        Unit
    }
}
