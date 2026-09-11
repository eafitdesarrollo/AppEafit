package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.AppNotification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val NOTIFICATIONS_COLLECTION = "notifications"

class NotificationRepository(private val firestore: FirebaseFirestore) {

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

        (personal + broadcast + roleTargeted).distinctBy { it.id }.sortedByDescending { it.createdAt }
    }

    suspend fun broadcast(title: String, body: String, targetRole: String): Result<Unit> = runCatching {
        firestore.collection(NOTIFICATIONS_COLLECTION).add(
            mapOf(
                "title" to title,
                "body" to body,
                "targetRole" to targetRole,
                "targetUserId" to "",
                "read" to false,
                "createdAt" to System.currentTimeMillis()
            )
        ).await()
        Unit
    }

    suspend fun markRead(id: String): Result<Unit> = runCatching {
        firestore.collection(NOTIFICATIONS_COLLECTION).document(id).update("read", true).await()
        Unit
    }
}
