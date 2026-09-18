package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.CalendarEventDao
import co.edu.eafit.appeafit.data.local.entity.CachedCalendarEventEntity
import co.edu.eafit.appeafit.domain.model.CalendarEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val CALENDAR_COLLECTION = "calendarEvents"

class CalendarRepository(
    private val firestore: FirebaseFirestore,
    private val calendarEventDao: CalendarEventDao
) {
    private fun CachedCalendarEventEntity.toDomain() = CalendarEvent(id, title, description, date)
    private fun CalendarEvent.toEntity() = CachedCalendarEventEntity(id, title, description, date)

    fun observeCached(): Flow<List<CalendarEvent>> =
        calendarEventDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun refresh(): Result<Unit> = runCatching {
        val events = firestore.collection(CALENDAR_COLLECTION)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .await()
            .documents
            .map { it.toCalendarEvent() }
        // Limpia antes de insertar: si no, un evento borrado en Firestore quedaba
        // "fantasma" para siempre en el caché local (nunca se eliminaba).
        calendarEventDao.clear()
        calendarEventDao.upsertAll(events.map { it.toEntity() })
    }

    suspend fun createEvent(event: CalendarEvent): Result<Unit> = runCatching {
        firestore.collection(CALENDAR_COLLECTION).add(event.toMap()).await()
        Unit
    }

    suspend fun updateEvent(event: CalendarEvent): Result<Unit> = runCatching {
        firestore.collection(CALENDAR_COLLECTION).document(event.id).set(event.toMap()).await()
        Unit
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        firestore.collection(CALENDAR_COLLECTION).document(eventId).delete().await()
        Unit
    }
}
