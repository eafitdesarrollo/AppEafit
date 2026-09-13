package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val NOTIFICATIONS_COLLECTION = "notifications"
private const val READS_SUBCOLLECTION = "reads"

class NotificationRepository(private val firestore: FirebaseFirestore) {

    /**
     * El estado "leído" YA NO vive en el documento de la notificación (que es compartido
     * por todos sus destinatarios): antes, el primer usuario que abría un broadcast lo
     * marcaba como leído para todos los demás. Ahora cada usuario tiene su propio marcador
     * en la subcolección notifications/{id}/reads/{uid}, y aquí se combina con el contenido
     * para armar el estado "leído" real de ESTE usuario.
     */
    suspend fun listFor(userId: String, role: String): Result<List<AppNotification>> = runCatching {
        val personal = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("targetUserId", userId)
            .get()
            .await()
            .documents
            .map { it.toAppNotification() }

        val broadcast = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("targetRole", "all")
            .get()
            .await()
            .documents
            .map { it.toAppNotification() }

        val roleTargeted = firestore.collection(NOTIFICATIONS_COLLECTION)
            .whereEqualTo("targetRole", role)
            .get()
            .await()
            .documents
            .map { it.toAppNotification() }

        val notifications = (personal + broadcast + roleTargeted).distinctBy { it.id }

        val readNotificationIds = firestore.collectionGroup(READS_SUBCOLLECTION)
            .whereEqualTo("uid", userId)
            .get()
            .await()
            .documents
            .mapNotNull { it.reference.parent.parent?.id }
            .toSet()

        notifications
            .map { it.copy(read = it.id in readNotificationIds) }
            .sortedByDescending { it.createdAt }
    }

    suspend fun broadcast(title: String, body: String, targetRole: String): Result<Unit> = runCatching {
        firestore.collection(NOTIFICATIONS_COLLECTION).add(
            mapOf(
                "title" to title,
                "body" to body,
                "targetRole" to targetRole,
                "targetUserId" to "",
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Unit
    }

    suspend fun markRead(notificationId: String, userId: String): Result<Unit> = runCatching {
        firestore.collection(NOTIFICATIONS_COLLECTION).document(notificationId)
            .collection(READS_SUBCOLLECTION).document(userId)
            .set(mapOf("uid" to userId, "readAt" to System.currentTimeMillis()))
            .await()
        Unit
    }
}
