package co.edu.eafit.appeafit.domain.model

data class Loan(
    val id: String = "",
    val studentId: String = "",
    val itemTitle: String = "",
    val loanedAt: Long = 0L,
    val dueAt: Long = 0L,
    val returned: Boolean = false
)
