package co.edu.eafit.appeafit.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.util.toFriendlyAuthMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

data class PasswordUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {

    private val _editState = MutableStateFlow(ProfileEditUiState())
    val editState: StateFlow<ProfileEditUiState> = _editState

    private val _passwordState = MutableStateFlow(PasswordUiState())
    val passwordState: StateFlow<PasswordUiState> = _passwordState

    fun updateProfile(uid: String, fullName: String, program: String, photoUri: Uri?) {
        _editState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            var photoUrl = ""
            if (photoUri != null) {
                // 2026-09-17: reemplazo temporal de Firebase Storage (ver ImageKitClient y
                // BITACORA). useUniqueFileName=false + mismo folder/nombre por uid hace que
                // ImageKit sobreescriba la foto anterior de este usuario en el mismo lugar
                // (nunca queda una foto vieja huérfana al cambiarla).
                val uploadResult = container.imageKitClient.upload(
                    uri = photoUri,
                    folder = "appeafit/profile_photos",
                    fileName = "$uid.jpg",
                    useUniqueFileName = false
                )
                uploadResult.onSuccess { photoUrl = it.url }
                    .onFailure {
                        _editState.update { s -> s.copy(isSaving = false, errorMessage = "No se pudo subir la foto") }
                        return@launch
                    }
            }
            val currentPhoto = if (photoUri != null) photoUrl else container.userRepository.getCachedUser(uid)?.photoUrl.orEmpty()
            val result = container.userRepository.updateProfile(uid, fullName, program, currentPhoto)
            result.onSuccess {
                _editState.update { it.copy(isSaving = false, saved = true) }
            }.onFailure {
                _editState.update { s -> s.copy(isSaving = false, errorMessage = it.message ?: "No se pudo guardar") }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (currentPassword.isBlank()) {
            _passwordState.update { it.copy(errorMessage = "Ingresa tu contraseña actual") }
            return
        }
        if (newPassword.length < 6) {
            _passwordState.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (newPassword != confirmPassword) {
            _passwordState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        _passwordState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = container.authRepository.changePassword(currentPassword, newPassword)
            result.onSuccess {
                _passwordState.update { it.copy(isSaving = false, success = true) }
            }.onFailure {
                _passwordState.update { s -> s.copy(isSaving = false, errorMessage = it.toFriendlyAuthMessage()) }
            }
        }
    }
}
