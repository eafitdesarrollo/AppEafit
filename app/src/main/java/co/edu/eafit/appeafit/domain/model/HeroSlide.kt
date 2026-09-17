package co.edu.eafit.appeafit.domain.model

/**
 * Slide del hero del sitio web (appeafit-web-frontend) e -- a futuro -- de la app de
 * escritorio: imagen o video en loop que Admin/Staff publican desde esta app. El campo
 * [mediaFileId] es de uso interno (nunca lo lee el sitio web, ver
 * web/appeafit-web-frontend/src/types.ts) -- solo sirve para poder borrar el archivo por
 * completo de ImageKit cuando se borra o se reemplaza el slide (ver BITACORA, regla de
 * borrado completo).
 */
data class HeroSlide(
    val id: String = "",
    val type: String = "image", // "image" | "video"
    val url: String = "",
    val mediaFileId: String = "",
    val title: String = "",
    val subtitle: String = "",
    val ctaLabel: String = "",
    val ctaHref: String = "",
    val order: Int = 0
)
