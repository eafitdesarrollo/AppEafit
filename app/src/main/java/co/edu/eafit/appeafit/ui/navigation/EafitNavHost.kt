package co.edu.eafit.appeafit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.core.di.GenericViewModelFactory
import co.edu.eafit.appeafit.ui.auth.AuthViewModel
import co.edu.eafit.appeafit.ui.auth.ForgotPasswordScreen
import co.edu.eafit.appeafit.ui.auth.LoginScreen
import co.edu.eafit.appeafit.ui.auth.RegisterScreen
import co.edu.eafit.appeafit.ui.splash.SplashScreen

@Composable
fun EafitNavHost(container: AppContainer) {
    val sessionViewModel: SessionViewModel = viewModel(factory = GenericViewModelFactory { SessionViewModel(container) })
    val authViewModel: AuthViewModel = viewModel(factory = GenericViewModelFactory { AuthViewModel(container) })

    val sessionState by sessionViewModel.sessionState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by sessionViewModel.isOnline.collectAsStateWithLifecycle()

    val navController = rememberNavController()

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is SessionState.LoggedOut -> navigateAndClear(navController, Routes.LOGIN)
            is SessionState.LoggedIn -> navigateAndClear(navController, Routes.MAIN)
            SessionState.CheckingSession -> Unit
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) { SplashScreen() }

        composable(Routes.LOGIN) {
            LoginScreen(
                uiState = authUiState,
                onLogin = { email, password -> authViewModel.login(email, password) {} },
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
                onGoToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                uiState = authUiState,
                onRegister = { fullName, email, studentId, program, password, confirm ->
                    authViewModel.register(fullName, email, studentId, program, password, confirm) {}
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                uiState = authUiState,
                onSendReset = { authViewModel.sendPasswordReset(it) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MAIN) {
            val state = sessionState
            if (state is SessionState.LoggedIn) {
                MainScreen(
                    container = container,
                    user = state.user,
                    isOnline = isOnline,
                    navController = navController,
                    onSignOut = { sessionViewModel.signOut() }
                )
            }
        }

        val currentUser: () -> co.edu.eafit.appeafit.domain.model.User? = {
            (sessionState as? SessionState.LoggedIn)?.user
        }

        studentGraph(container, navController, currentUser)
        professorGraph(container, navController, currentUser)
        staffGraph(container, navController, currentUser)
        adminGraph(container, navController, currentUser)
        commonGraph(container, navController, currentUser)
    }
}

private fun navigateAndClear(navController: NavHostController, route: String) {
    if (navController.currentDestination?.route == route) return
    navController.navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
