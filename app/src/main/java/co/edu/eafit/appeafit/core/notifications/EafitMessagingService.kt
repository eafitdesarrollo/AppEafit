package co.edu.eafit.appeafit.core.notifications

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.edu.eafit.appeafit.R
import android.util.Log
import co.edu.eafit.appeafit.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class EafitMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"] ?: return
        // Antes el switch "Notificaciones push" de Configuración guardaba una preferencia
        // que ningún otro archivo del proyecto leía — no hacía nada. Ahora se respeta aquí:
        // si el usuario lo desactivó, no se muestra el aviso en la bandeja del sistema.
        val notificationsEnabled = runBlocking { SettingsRepository(applicationContext).notificationsEnabled.first() }
        if (!notificationsEnabled) return
        showNotification(title, body)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // .update() falla si users/{uid} todavía no existe (p. ej. el token llega justo
        // después de crear la cuenta en Auth pero antes de crear el documento de perfil en
        // Firestore). Antes esa falla era 100% silenciosa (sin log, sin reintento) y el
        // dispositivo se quedaba sin push hasta el próximo refresco de token; ahora al
        // menos queda logueada, y UserRepository.createUserProfile() reintenta guardar el
        // token vigente justo después de crear el perfil para cubrir esa carrera.
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e -> Log.w("EafitMessagingService", "No se pudo guardar el fcmToken (perfil aún no existe)", e) }
    }

    private fun showNotification(title: String, body: String) {
        val hasPermission = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
    }
}
