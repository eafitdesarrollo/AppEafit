package co.edu.eafit.appeafit.domain.model

data class NewsItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val body: String = "",
    val imageUrl: String = "",
    // 2026-09-17: id del archivo en ImageKit (proveedor temporal, ver BITACORA) -- se
    // necesita para poder borrar la imagen por completo cuando se borra el anuncio.
    val imageFileId: String = "",
    val authorId: String = "",
    val publishedAt: Long = 0L
)
