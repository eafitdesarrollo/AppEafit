package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.domain.model.LostItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val LOST_ITEMS_COLLECTION = "lostItems"

class LostItemRepository(private val firestore: FirebaseFirestore) {

    suspend fun list(): Result<List<LostItem>> = runCatching {
        firestore.collection(LOST_ITEMS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toLostItem() }
    }

    suspend fun report(item: LostItem): Result<Unit> = runCatching {
        firestore.collection(LOST_ITEMS_COLLECTION).add(item.toMap()).await()
        Unit
    }

    suspend fun updateStatus(id: String, status: String): Result<Unit> = runCatching {
        firestore.collection(LOST_ITEMS_COLLECTION).document(id).update("status", status).await()
        Unit
    }
}
