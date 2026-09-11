package co.edu.eafit.appeafit.domain.model

data class NewsItem(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val body: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val publishedAt: Long = 0L
)
