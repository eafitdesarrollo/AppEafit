package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.NewsDao
import co.edu.eafit.appeafit.data.local.entity.CachedNewsEntity
import co.edu.eafit.appeafit.domain.model.NewsItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val NEWS_COLLECTION = "news"

class NewsRepository(
    private val firestore: FirebaseFirestore,
    private val newsDao: NewsDao
) {
    private fun CachedNewsEntity.toDomain() = NewsItem(id, title, category, body, imageUrl, authorId, publishedAt)
    private fun NewsItem.toEntity() = CachedNewsEntity(id, title, category, body, imageUrl, authorId, publishedAt)

    fun observeCached(): Flow<List<NewsItem>> = newsDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun refresh(): Result<Unit> = runCatching {
        val docs = firestore.collection(NEWS_COLLECTION)
            .orderBy("publishedAt", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .await()
        val items = docs.documents.map { it.toNewsItem() }
        // Limpia antes de insertar: si no, una noticia borrada en Firestore quedaba
        // "fantasma" para siempre en el caché local (nunca se eliminaba).
        newsDao.clear()
        newsDao.upsertAll(items.map { it.toEntity() })
    }

    suspend fun publish(item: NewsItem): Result<Unit> = runCatching {
        firestore.collection(NEWS_COLLECTION).add(item.toMap()).await()
        Unit
    }

    suspend fun delete(id: String): Result<Unit> = runCatching {
        firestore.collection(NEWS_COLLECTION).document(id).delete().await()
        Unit
    }
}
