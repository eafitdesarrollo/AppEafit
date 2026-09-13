package co.edu.eafit.appeafit.data.repository

import co.edu.eafit.appeafit.data.local.dao.UserDao
import co.edu.eafit.appeafit.data.local.entity.CachedUserEntity
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val USERS_COLLECTION = "users"

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) {
    private fun CachedUserEntity.toUser() = User(
        uid = uid,
        email = email,
        fullName = fullName,
        role = Role.fromId(role),
        program = program,
        institutionalId = institutionalId,
        photoUrl = photoUrl,
        active = active,
        createdAt = createdAt
    )

    private fun User.toEntity() = CachedUserEntity(
        uid = uid,
        email = email,
        fullName = fullName,
        role = role.id,
        program = program,
        institutionalId = institutionalId,
        photoUrl = photoUrl,
        active = active,
        createdAt = createdAt
    )

    fun observeCachedUser(uid: String): Flow<User?> = userDao.observe(uid).map { it?.toUser() }

    suspend fun getCachedUser(uid: String): User? = userDao.get(uid)?.toUser()

    suspend fun createUserProfile(user: User): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(user.uid).set(user.toMap()).await()
        userDao.upsert(user.toEntity())
        // El token FCM puede haber llegado (EafitMessagingService.onNewToken) ANTES de que
        // este documento existiera, en cuyo caso ese update se perdió silenciosamente.
        // Se reintenta aquí, ya con el documento creado; si falla (p. ej. sin Play Services
        // en el emulador) se ignora, no es crítico para completar el registro.
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNotBlank()) {
                firestore.collection(USERS_COLLECTION).document(user.uid).update("fcmToken", token).await()
            }
        }
    }

    suspend fun refreshUser(uid: String): Result<User> = runCatching {
        val snapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
        val user = snapshot.toUser()
        userDao.upsert(user.toEntity())
        user
    }

    suspend fun updateProfile(uid: String, fullName: String, program: String, photoUrl: String): Result<Unit> =
        runCatching {
            val updates = mapOf(
                "fullName" to fullName,
                "program" to program,
                "photoUrl" to photoUrl
            )
            firestore.collection(USERS_COLLECTION).document(uid).update(updates).await()
            val cached = userDao.get(uid)
            if (cached != null) {
                userDao.upsert(cached.copy(fullName = fullName, program = program, photoUrl = photoUrl))
            }
        }

    suspend fun listUsers(): Result<List<User>> = runCatching {
        firestore.collection(USERS_COLLECTION).get().await().documents.map { it.toUser() }
    }

    suspend fun updateRole(uid: String, role: Role): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(uid).update("role", role.id).await()
        Unit
    }

    suspend fun setActive(uid: String, active: Boolean): Result<Unit> = runCatching {
        firestore.collection(USERS_COLLECTION).document(uid).update("active", active).await()
        Unit
    }

    suspend fun clearCache() = userDao.clear()
}
