package co.edu.eafit.appeafit.data.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {

    val currentUserId: String? get() = auth.currentUser?.uid

    val authState: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser != null) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        Unit
    }

    suspend fun register(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("No se pudo crear el usuario")
    }

    /**
     * Si falla la creación del documento de perfil en Firestore justo después de registrar
     * la cuenta en Auth, antes esta quedaba "huérfana" (viva en Auth, sin perfil) y
     * cualquier regla basada en myRole() fallaba, sin ninguna forma de recuperarse salvo
     * borrar la cuenta manualmente desde la consola de Firebase. Se usa para revertir ese
     * registro fallido y dejar al usuario en condiciones de reintentar desde cero.
     */
    suspend fun deleteCurrentAccount(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
        Unit
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
        Unit
    }

    /**
     * Firebase exige un login "reciente" para cambiar la contraseña; por eso reautenticamos
     * con la contraseña actual antes de aplicar la nueva (si no, falla con
     * FirebaseAuthRecentLoginRequiredException en cualquier sesión que no sea recién iniciada).
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("Sesión no válida")
        val email = user.email ?: error("Sesión no válida")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
        Unit
    }

    fun signOut() = auth.signOut()
}
