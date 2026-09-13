package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.Reservation
import co.edu.eafit.appeafit.domain.model.ReservationStatus
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

    /**
     * Antes se creaba la reserva directo, sin ninguna consulta previa que revisara si ya
     * existía otra reserva pendiente/aprobada para el mismo espacio y fecha con horario
     * solapado — dos personas podían reservar la misma sala a la misma hora sin ningún
     * aviso, y quien las aprobara no tenía ninguna señal visual del conflicto.
     */
    suspend fun create(reservation: Reservation): Result<Unit> = runCatching {
        val sameDaySameSpace = firestore.collection(RESERVATIONS_COLLECTION)
            .whereEqualTo("spaceId", reservation.spaceId)
            .whereEqualTo("date", reservation.date)
            .get()
            .await()
            .documents
            .map { it.toReservation() }

        val overlaps = sameDaySameSpace.any { existing ->
            existing.status != ReservationStatus.REJECTED.id &&
                existing.status != ReservationStatus.CANCELLED.id &&
                reservation.startTime < existing.endTime && reservation.endTime > existing.startTime
        }
        if (overlaps) {
            error("Ya hay una reserva pendiente o aprobada para ese espacio en ese horario")
        }
        firestore.collection(RESERVATIONS_COLLECTION).add(reservation.toMap()).await()
        Unit
    }

    suspend fun updateStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection(RESERVATIONS_COLLECTION).document(id).update("status", status).await()
        Unit
    }
}
