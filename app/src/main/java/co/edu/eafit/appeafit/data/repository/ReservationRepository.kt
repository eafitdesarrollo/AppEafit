package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.Reservation
import co.edu.eafit.appeafit.domain.model.Space
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val SPACES_COLLECTION = "spaces"
private const val RESERVATIONS_COLLECTION = "reservations"

class ReservationRepository(private val firestore: FirebaseFirestore) {

    suspend fun listSpaces(): Result<List<Space>> = runCatching {
        firestore.collection(SPACES_COLLECTION).get().await().documents.map { it.toSpace() }
    }

    suspend fun listForUser(userId: String): Result<List<Reservation>> = runCatching {
        firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toReservation() }
    }

    suspend fun listAll(): Result<List<Reservation>> = runCatching {
        firestore.collection(RESERVATIONS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toReservation() }
    }

    suspend fun create(reservation: Reservation): Result<Unit> = runCatching {
        firestore.collection(RESERVATIONS_COLLECTION).add(reservation.toMap()).await()
        Unit
    }

    suspend fun updateStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection(RESERVATIONS_COLLECTION).document(id).update("status", status).await()
        Unit
    }
}
