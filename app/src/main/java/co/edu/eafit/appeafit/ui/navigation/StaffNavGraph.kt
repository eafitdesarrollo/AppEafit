package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.staff.DirectoryScreen
import co.edu.eafit.appeafit.ui.staff.ManageAnnouncementsScreen
import co.edu.eafit.appeafit.ui.student.LostItemsScreen
import co.edu.eafit.appeafit.ui.student.SpaceReservationScreen

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
    composable(Routes.STAFF_MANAGE_RESERVATIONS) {
        val user = currentUser() ?: return@composable
        SpaceReservationScreen(container, user.uid, user.fullName, manageAll = true) { navController.popBackStack() }
    }
    composable(Routes.STAFF_MANAGE_LOST_ITEMS) {
        val user = currentUser() ?: return@composable
        LostItemsScreen(container, user.uid, canManage = true) { navController.popBackStack() }
    }
}
