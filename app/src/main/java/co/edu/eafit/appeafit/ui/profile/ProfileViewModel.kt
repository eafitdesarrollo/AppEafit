package co.edu.eafit.appeafit.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
                val uploadResult = runCatching {
                    val ref = container.storage.reference.child("profile_photos/$uid.jpg")
                    ref.putFile(photoUri).await()
                    ref.downloadUrl.await().toString()
                }
                uploadResult.onSuccess { photoUrl = it }
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

    fun changePassword(newPassword: String, confirmPassword: String) {
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
            val result = container.authRepository.changePassword(newPassword)
            result.onSuccess {
                _passwordState.update { it.copy(isSaving = false, success = true) }
            }.onFailure {
                _passwordState.update { s -> s.copy(isSaving = false, errorMessage = it.message ?: "No se pudo cambiar la contraseña") }
            }
        }
    }
}
