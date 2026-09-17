package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.core.imagekit.ImageKitClient
import co.edu.eafit.appeafit.domain.model.HeroSlide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val HERO_SLIDES_COLLECTION = "hero_slides"

/**
 * A diferencia de NewsRepository, no mantiene caché local en Room: hero_slides es una
 * colección pequeña (unos pocos slides) que solo se administra desde esta pantalla, que
 * siempre está en línea cuando se usa -- se refresca directo de Firestore cada vez.
 */
class HeroSlideRepository(
    private val firestore: FirebaseFirestore,
    private val imageKitClient: ImageKitClient
) {
    suspend fun fetchAll(): Result<List<HeroSlide>> = runCatching {
        firestore.collection(HERO_SLIDES_COLLECTION)
            .orderBy("order", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toHeroSlide() }
    }

    suspend fun publish(slide: HeroSlide): Result<Unit> = runCatching {
        firestore.collection(HERO_SLIDES_COLLECTION).add(slide.toMap()).await()
        Unit
    }

    suspend fun update(slide: HeroSlide): Result<Unit> = runCatching {
        firestore.collection(HERO_SLIDES_COLLECTION).document(slide.id).set(slide.toMap()).await()
        Unit
    }

    /**
     * Borra el slide de Firestore Y su imagen/video en ImageKit -- misma regla de borrado
     * completo que NewsRepository.delete() (ver BITACORA).
     */
    suspend fun delete(slide: HeroSlide): Result<Unit> = runCatching {
        if (slide.mediaFileId.isNotBlank()) {
            imageKitClient.delete(slide.mediaFileId)
        }
        firestore.collection(HERO_SLIDES_COLLECTION).document(slide.id).delete().await()
        Unit
    }
}
