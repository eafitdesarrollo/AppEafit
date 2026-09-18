package co.edu.eafit.appeafit.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

private const val CONTACT_MESSAGES_COLLECTION = "contact_messages"

/**
 * Mensajes del formulario de contacto del sitio web (appeafit-web-backend),
 * colección compartida `contact_messages` -- las reglas de Firestore ya
 * permitían a staff/admin leer/actualizar/borrar esta colección desde que
 * se creó (2026-09-17), pero nunca existió una pantalla en la app para
 * consultarlos. Ver BITACORA.
 */
class ContactMessageRepository(private val firestore: FirebaseFirestore) {

    suspend fun listAll() = runCatching {
        firestore.collection(CONTACT_MESSAGES_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents
            .map { it.toContactMessage() }
    }

    suspend fun setResolved(messageId: String, resolved: Boolean): Result<Unit> = runCatching {
        firestore.collection(CONTACT_MESSAGES_COLLECTION).document(messageId)
            .update("resolved", resolved)
            .await()
        Unit
    }

    suspend fun delete(messageId: String): Result<Unit> = runCatching {
        firestore.collection(CONTACT_MESSAGES_COLLECTION).document(messageId).delete().await()
        Unit
    }
}
