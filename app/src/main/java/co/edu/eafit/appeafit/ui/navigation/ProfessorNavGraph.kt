package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.professor.AnnouncementsScreen
import co.edu.eafit.appeafit.ui.professor.AttendanceScreen
import co.edu.eafit.appeafit.ui.professor.GradeEntryScreen
import co.edu.eafit.appeafit.ui.professor.MyCoursesScreen

fun NavGraphBuilder.professorGraph(
    container: AppContainer,
    navController: NavHostController,
    currentUser: () -> User?
) {
    composable(Routes.PROFESSOR_MY_COURSES) {
        val user = currentUser() ?: return@composable
        MyCoursesScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.PROFESSOR_ATTENDANCE) {
        val user = currentUser() ?: return@composable
        AttendanceScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.PROFESSOR_GRADE_ENTRY) {
        val user = currentUser() ?: return@composable
        GradeEntryScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.PROFESSOR_ANNOUNCEMENTS) {
        val user = currentUser() ?: return@composable
        AnnouncementsScreen(container, user.uid) { navController.popBackStack() }
    }
}
