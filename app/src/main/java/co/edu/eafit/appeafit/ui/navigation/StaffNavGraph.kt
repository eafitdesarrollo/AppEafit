package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.staff.DirectoryScreen
import co.edu.eafit.appeafit.ui.staff.ManageAnnouncementsScreen
import co.edu.eafit.appeafit.ui.staff.ManageHeroSlidesScreen

fun NavGraphBuilder.staffGraph(
    container: AppContainer,
    navController: NavHostController,
    currentUser: () -> User?
) {
    composable(Routes.STAFF_DIRECTORY) {
        DirectoryScreen(container) { navController.popBackStack() }
    }
    composable(Routes.STAFF_MANAGE_ANNOUNCEMENTS) {
        val user = currentUser() ?: return@composable
        ManageAnnouncementsScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.STAFF_MANAGE_HERO_SLIDES) {
        ManageHeroSlidesScreen(container) { navController.popBackStack() }
    }
}
