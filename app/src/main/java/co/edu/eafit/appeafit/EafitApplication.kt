package co.edu.eafit.appeafit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import co.edu.eafit.appeafit.core.di.AppContainer

class EafitApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    /**
     * Coil ya cachea en memoria y disco por defecto incluso sin esto, pero se deja
     * explícito (en vez de depender de los valores por defecto implícitos) porque con
     * muchos usuarios viendo las mismas fotos de perfil/noticias/hero slides a la vez,
     * cada `AsyncImage` debe resolver desde este caché compartido y no volver a pedirle
     * el archivo a ImageKit -- si no, con suficiente gente conectada a la vez esas
     * peticiones repetidas podrían verse como tráfico de bot para el proveedor y
     * bloquear el servicio para todos. `respectCacheHeaders` en true (el default) hace
     * que además se respete cualquier Cache-Control que ImageKit ya envíe.
     */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(cacheDir.resolve("image_cache"))
                .maxSizeBytes(100L * 1024 * 1024)
                .build()
        }
        .respectCacheHeaders(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.default_notification_channel_id),
                getString(R.string.default_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
