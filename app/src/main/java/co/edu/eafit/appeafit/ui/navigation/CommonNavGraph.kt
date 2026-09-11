package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.notifications.NotificationsScreen
import co.edu.eafit.appeafit.ui.profile.ChangePasswordScreen
import co.edu.eafit.appeafit.ui.profile.EditProfileScreen
import co.edu.eafit.appeafit.ui.profile.SettingsScreen

fun NavGraphBuilder.commonGraph(
    container: AppContainer,
    navController: NavHostController,
    currentUser: () -> User?
) {
    composable(Routes.EDIT_PROFILE) {
        val user = currentUser() ?: return@composable
        EditProfileScreen(container = container, user = user, onBack = { navController.popBackStack() })
    }
    composable(Routes.CHANGE_PASSWORD) {
        ChangePasswordScreen(container = container, onBack = { navController.popBackStack() })
    }
    composable(Routes.SETTINGS) {
        SettingsScreen(container = container, onBack = { navController.popBackStack() })
    }
    composable(Routes.NOTIFICATIONS) {
        val user = currentUser() ?: return@composable
        NotificationsScreen(container = container, user = user, onBack = { navController.popBackStack() })
    }
}
