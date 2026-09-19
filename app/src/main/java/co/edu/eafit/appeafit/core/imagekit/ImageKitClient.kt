package co.edu.eafit.appeafit.core.imagekit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class ImageUploadResult(val url: String, val fileId: String)

/**
 * 2026-09-17: reemplazo TEMPORAL de Firebase Storage (desactivado hasta que el cliente
 * pague el plan Blaze) usando ImageKit.io (cuenta gratuita de eafitdesarrollo@gmail.com,
 * 20GB/mes). Ver BITACORA.md para la constancia completa de esta decisión y cuándo
 * revertir a Firebase Storage.
 *
 * ImageKit exige firmar cada subida (HMAC-SHA1 de token+expire con la clave privada de
 * la cuenta) -- normalmente esa firma se genera en un backend para no exponer la clave
 * privada, pero este proyecto no tiene backend propio (solo Firebase Auth/Firestore, sin
 * Cloud Functions). Se firma aquí mismo, en el cliente, con la clave privada embebida:
 * mismo nivel de riesgo ya aceptado y documentado en este proyecto para el QR del carnet
 * (ver sección 6 de BITACORA, "QR del carnet digital falsificable"). Aceptable para esta
 * etapa de demo con las 4 cuentas de prueba; si este proveedor se usara en producción de
 * verdad, la firma debe moverse a un backend real.
 */
class ImageKitClient(private val context: Context) {

    // Timeouts explícitos: el cliente por defecto de OkHttp ya tiene 10s de conexión/
    // lectura/escritura, pero se dejan explícitos aquí (con algo más de margen para
    // escritura, ya que subir una imagen puede tardar más que una llamada normal a la
    // API) para que un usuario nunca se quede viendo un spinner indefinidamente si la
    // red está caída -- después de este tiempo, upload() falla con un error claro en
    // vez de colgarse para siempre.
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun signature(token: String, expire: Long): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(PRIVATE_KEY.toByteArray(Charsets.UTF_8), "HmacSHA1"))
        val raw = mac.doFinal((token + expire).toByteArray(Charsets.UTF_8))
        return raw.joinToString("") { "%02x".format(it) }
    }

    /**
     * Reduce la imagen a como máximo [maxDimension] px de lado más largo y la recomprime
     * a JPEG -- una foto tomada con la cámara del celular puede pesar 5-15MB a resolución
     * completa, y subir eso tal cual (sin comprimir) es lo que hacía que "guardar cambios"
     * en el perfil se sintiera colgado durante mucho tiempo en redes lentas. Se decodifica
     * primero solo los bounds (inJustDecodeBounds) para calcular inSampleSize y no cargar
     * el bitmap completo en memoria si no hace falta (evita OutOfMemoryError con fotos
     * muy grandes). Si algo falla al decodificar (ej. es un video, no una imagen), se
     * devuelve null y el llamador sube los bytes originales sin tocar.
     */
    private fun compressImage(bytes: ByteArray, maxDimension: Int): ByteArray? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= maxDimension && bounds.outHeight / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            ?: return null

        val scale = maxDimension.toFloat() / maxOf(decoded.width, decoded.height)
        val resized = if (scale < 1f) {
            Bitmap.createScaledBitmap(decoded, (decoded.width * scale).toInt(), (decoded.height * scale).toInt(), true)
        } else {
            decoded
        }

        ByteArrayOutputStream().use { output ->
            resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
            if (resized !== decoded) decoded.recycle()
            resized.recycle()
            output.toByteArray()
        }
    }.getOrNull()

    /**
     * Sube [uri] a la carpeta [folder] con el nombre [fileName]. Con
     * [useUniqueFileName] = false y el mismo folder+fileName de una subida anterior,
     * ImageKit sobreescribe el archivo viejo en el mismo lugar (no queda ningún archivo
     * huérfano) -- se usa así para fotos de perfil, donde cada usuario tiene un slot fijo.
     * Para imágenes nuevas de verdad (anuncios) se deja useUniqueFileName = true y se
     * guarda el fileId devuelto para poder borrar el archivo explícitamente más adelante.
     *
     * [compressImage] debe ser `false` para archivos que no son imágenes estáticas (ej.
     * los videos de hero slides) -- para el resto, se recomprime antes de subir (ver
     * [compressImage] función privada) para no depender de subir la foto a resolución de
     * cámara completa.
     */
    suspend fun upload(
        uri: Uri,
        folder: String,
        fileName: String,
        useUniqueFileName: Boolean = true,
        compressImage: Boolean = true,
        maxDimension: Int = 1280
    ): Result<ImageUploadResult> = withContext(Dispatchers.IO) {
        runCatching {
            val originalBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("No se pudo leer la imagen seleccionada")
            val bytes = if (compressImage) compressImage(originalBytes, maxDimension) ?: originalBytes else originalBytes
            val token = UUID.randomUUID().toString()
            val expire = (System.currentTimeMillis() / 1000) + 2400
            val sig = signature(token, expire)

            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, bytes.toRequestBody("application/octet-stream".toMediaType()))
                .addFormDataPart("fileName", fileName)
                .addFormDataPart("folder", folder)
                .addFormDataPart("useUniqueFileName", useUniqueFileName.toString())
                .addFormDataPart("publicKey", PUBLIC_KEY)
                .addFormDataPart("signature", sig)
                .addFormDataPart("expire", expire.toString())
                .addFormDataPart("token", token)
                .build()

            val request = Request.Builder()
                .url("https://upload.imagekit.io/api/v1/files/upload")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("ImageKit upload failed (${response.code}): $responseBody")
                val json = JSONObject(responseBody)
                ImageUploadResult(url = json.getString("url"), fileId = json.getString("fileId"))
            }
        }
    }

    /**
     * Borra por completo el archivo [fileId] de ImageKit. Se llama siempre que se borra
     * el dato dueño de la imagen (un anuncio, por ejemplo) -- cumpliendo la regla de esta
     * bitácora de que borrar algo lo borra por completo de donde esté guardado, no solo
     * de Firestore.
     */
    suspend fun delete(fileId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (fileId.isBlank()) return@runCatching
            val request = Request.Builder()
                .url("https://api.imagekit.io/v1/files/$fileId")
                .delete()
                .header("Authorization", Credentials.basic(PRIVATE_KEY, ""))
                .build()
            client.newCall(request).execute().use { response ->
                // 404 = ya no existe (por ejemplo, un reintento) -- no es un error real.
                if (!response.isSuccessful && response.code != 404) {
                    error("ImageKit delete failed (${response.code}): ${response.body?.string()}")
                }
            }
        }
    }

    companion object {
        const val URL_ENDPOINT = "https://ik.imagekit.io/eafit"
        private const val PUBLIC_KEY = "public_/zYNXHjE7L0auLM8aztCJKLBuNE="
        private const val PRIVATE_KEY = "private_nYm6sORHokFE99uPmj6CqFplnkQ="
    }
}
