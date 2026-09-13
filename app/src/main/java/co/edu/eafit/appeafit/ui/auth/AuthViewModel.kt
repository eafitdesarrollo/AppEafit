package co.edu.eafit.appeafit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.util.toFriendlyAuthMessage
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val INSTITUTIONAL_DOMAIN = "@eafit.edu.co"

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class AuthViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun isInstitutionalEmail(email: String) = email.trim().endsWith(INSTITUTIONAL_DOMAIN, ignoreCase = true)

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        val trimmedEmail = email.trim()
        if (!isInstitutionalEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Debes usar tu correo institucional $INSTITUTIONAL_DOMAIN") }
            return
        }
        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu contraseña") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = container.authRepository.signIn(trimmedEmail, password)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, errorMessage = throwable.toFriendlyMessage()) }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        institutionalId: String,
        program: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val trimmedEmail = email.trim()
        when {
            fullName.isBlank() || institutionalId.isBlank() || program.isBlank() ->
                _uiState.update { it.copy(errorMessage = "Completa todos los campos") }
            !isInstitutionalEmail(trimmedEmail) ->
                _uiState.update { it.copy(errorMessage = "Debes usar tu correo institucional $INSTITUTIONAL_DOMAIN") }
            password.length < 6 ->
                _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            password != confirmPassword ->
                _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            else -> {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                viewModelScope.launch {
                    val registerResult = container.authRepository.register(trimmedEmail, password)
                    registerResult.onSuccess { uid ->
                        val newUser = User(
                            uid = uid,
                            email = trimmedEmail,
                            fullName = fullName,
                            role = Role.STUDENT,
                            program = program,
                            institutionalId = institutionalId,
                            createdAt = System.currentTimeMillis()
                        )
                        val profileResult = container.userRepository.createUserProfile(newUser)
                        profileResult.onSuccess {
                            _uiState.update { it.copy(isLoading = false) }
                            onSuccess()
                        }.onFailure { throwable ->
                            // Revierte la cuenta de Auth recién creada para no dejarla huérfana
                            // (viva en Auth, sin documento de perfil en Firestore).
                            container.authRepository.deleteCurrentAccount()
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = throwable.toFriendlyMessage())
                            }
                        }
                    }.onFailure { throwable ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = throwable.toFriendlyMessage()) }
                    }
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val trimmedEmail = email.trim()
        if (!isInstitutionalEmail(trimmedEmail)) {
            _uiState.update { it.copy(errorMessage = "Debes usar tu correo institucional $INSTITUTIONAL_DOMAIN") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        viewModelScope.launch {
            val result = container.authRepository.sendPasswordReset(trimmedEmail)
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, infoMessage = "Enlace enviado. Revisa tu correo institucional.") }
            }.onFailure { throwable ->
                _uiState.update { it.copy(isLoading = false, errorMessage = throwable.toFriendlyMessage()) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}

private fun Throwable.toFriendlyMessage(): String = toFriendlyAuthMessage()
