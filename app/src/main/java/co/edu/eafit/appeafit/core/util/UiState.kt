package co.edu.eafit.appeafit.core.util

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T, val fromCache: Boolean = false) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

sealed interface OpResult {
    data object Success : OpResult
    data class Error(val message: String) : OpResult
}
