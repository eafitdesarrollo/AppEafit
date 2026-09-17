package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.admin.BroadcastScreen
import co.edu.eafit.appeafit.ui.admin.ManageUsersScreen
import co.edu.eafit.appeafit.ui.admin.StatsScreen
import co.edu.eafit.appeafit.ui.staff.ManageAnnouncementsScreen
import co.edu.eafit.appeafit.ui.staff.ManageHeroSlidesScreen

fun NavGraphBuilder.adminGraph(
    container: AppContainer,
    navController: NavHostController,
    currentUser: () -> User?
) {
    composable(Routes.ADMIN_MANAGE_USERS) {
        ManageUsersScreen(container) { navController.popBackStack() }
    }
    composable(Routes.ADMIN_MANAGE_CONTENT) {
        val user = currentUser() ?: return@composable
        ManageAnnouncementsScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.ADMIN_MANAGE_HERO_SLIDES) {
        ManageHeroSlidesScreen(container) { navController.popBackStack() }
    }
    composable(Routes.ADMIN_BROADCAST) {
        BroadcastScreen(container) { navController.popBackStack() }
    }
    composable(Routes.ADMIN_STATS) {
        StatsScreen(container) { navController.popBackStack() }
    }
}
