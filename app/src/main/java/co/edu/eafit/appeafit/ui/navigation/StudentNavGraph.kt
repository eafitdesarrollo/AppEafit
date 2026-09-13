package co.edu.eafit.appeafit.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import co.edu.eafit.appeafit.core.di.AppContainer
import co.edu.eafit.appeafit.domain.model.User
import co.edu.eafit.appeafit.ui.student.AcademicCalendarScreen
import co.edu.eafit.appeafit.ui.student.GpaCalculatorScreen
import co.edu.eafit.appeafit.ui.student.GradesScreen
import co.edu.eafit.appeafit.ui.student.LibraryScreen
import co.edu.eafit.appeafit.ui.student.ScheduleScreen
import co.edu.eafit.appeafit.ui.student.TeacherEvaluationScreen

fun NavGraphBuilder.studentGraph(
    container: AppContainer,
    navController: NavHostController,
    currentUser: () -> User?
) {
    composable(Routes.STUDENT_GRADES) {
        val user = currentUser() ?: return@composable
        GradesScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.STUDENT_SCHEDULE) {
        val user = currentUser() ?: return@composable
        ScheduleScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.STUDENT_GPA_CALCULATOR) {
        GpaCalculatorScreen { navController.popBackStack() }
    }
    composable(Routes.STUDENT_ACADEMIC_CALENDAR) {
        AcademicCalendarScreen(container) { navController.popBackStack() }
    }
    composable(Routes.STUDENT_TEACHER_EVALUATION) {
        val user = currentUser() ?: return@composable
        TeacherEvaluationScreen(container, user.uid) { navController.popBackStack() }
    }
    composable(Routes.STUDENT_LIBRARY) {
        val user = currentUser() ?: return@composable
        LibraryScreen(container, user.uid) { navController.popBackStack() }
    }
}
