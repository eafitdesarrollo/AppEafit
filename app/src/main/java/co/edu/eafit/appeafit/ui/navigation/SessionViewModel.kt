package co.edu.eafit.appeafit.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object CheckingSession : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

class SessionViewModel(private val container: AppContainer) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessionState: StateFlow<SessionState> = container.authRepository.authState
        .flatMapLatest { isAuthenticated ->
            val uid = container.authRepository.currentUserId
            if (!isAuthenticated || uid == null) {
                flowOf<SessionState>(SessionState.LoggedOut)
            } else {
                refreshUserInBackground(uid)
                container.userRepository.observeCachedUser(uid).let { flow ->
                    kotlinx.coroutines.flow.channelFlow {
                        flow.collect { cached ->
                            send(if (cached != null) SessionState.LoggedIn(cached) else SessionState.CheckingSession)
                        }
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.CheckingSession)

    val isOnline: StateFlow<Boolean> = container.connectivityObserver.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    private fun refreshUserInBackground(uid: String) {
        viewModelScope.launch {
            container.userRepository.refreshUser(uid)
        }
    }

    fun signOut() {
        container.authRepository.signOut()
    }
}
