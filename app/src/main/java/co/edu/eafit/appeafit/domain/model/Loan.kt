package co.edu.eafit.appeafit.domain.model

const val MAX_LOAN_RENEWALS = 2

data class Loan(
    val id: String = "",
    val studentId: String = "",
    val itemTitle: String = "",
    val loanedAt: Long = 0L,
    val dueAt: Long = 0L,
    val returned: Boolean = false,
    val renewalCount: Int = 0
) {
    val canRenew: Boolean get() = !returned && renewalCount < MAX_LOAN_RENEWALS
}
