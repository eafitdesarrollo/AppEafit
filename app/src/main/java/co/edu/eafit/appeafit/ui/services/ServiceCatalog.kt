package co.edu.eafit.appeafit.ui.services

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.ui.navigation.Routes

data class ServiceEntry(
    val icon: ImageVector,
    val labelResId: Int,
    val route: String,
    val groupResId: Int
)

object ServiceCatalog {

    fun forRole(role: Role): List<ServiceEntry> = when (role) {
        Role.STUDENT -> listOf(
            ServiceEntry(Icons.Filled.Assessment, R.string.service_grades, Routes.STUDENT_GRADES, R.string.service_group_academic),
            ServiceEntry(Icons.Filled.CalendarMonth, R.string.service_schedule, Routes.STUDENT_SCHEDULE, R.string.service_group_academic),
            ServiceEntry(Icons.Filled.Calculate, R.string.service_gpa_calculator, Routes.STUDENT_GPA_CALCULATOR, R.string.service_group_academic),
            ServiceEntry(Icons.Filled.RateReview, R.string.service_teacher_evaluation, Routes.STUDENT_TEACHER_EVALUATION, R.string.service_group_academic),
            ServiceEntry(Icons.Filled.EventAvailable, R.string.service_academic_calendar, Routes.STUDENT_ACADEMIC_CALENDAR, R.string.service_group_academic),
            ServiceEntry(Icons.AutoMirrored.Filled.MenuBook, R.string.service_library, Routes.STUDENT_LIBRARY, R.string.service_group_library)
        )
        Role.PROFESSOR -> listOf(
            ServiceEntry(Icons.Filled.Groups, R.string.service_my_courses, Routes.PROFESSOR_MY_COURSES, R.string.service_group_teaching),
            ServiceEntry(Icons.Filled.HowToReg, R.string.service_attendance, Routes.PROFESSOR_ATTENDANCE, R.string.service_group_teaching),
            ServiceEntry(Icons.AutoMirrored.Filled.Assignment, R.string.service_grade_entry, Routes.PROFESSOR_GRADE_ENTRY, R.string.service_group_teaching),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_announcements, Routes.PROFESSOR_ANNOUNCEMENTS, R.string.service_group_teaching)
        )
        Role.STAFF -> listOf(
            ServiceEntry(Icons.Filled.ContactMail, R.string.service_directory, Routes.STAFF_DIRECTORY, R.string.service_group_institutional),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_manage_announcements, Routes.STAFF_MANAGE_ANNOUNCEMENTS, R.string.service_group_institutional)
        )
        Role.ADMIN -> listOf(
            ServiceEntry(Icons.Filled.SupervisedUserCircle, R.string.service_manage_users, Routes.ADMIN_MANAGE_USERS, R.string.service_group_admin),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_manage_content, Routes.ADMIN_MANAGE_CONTENT, R.string.service_group_admin),
            ServiceEntry(Icons.AutoMirrored.Filled.Send, R.string.service_broadcast, Routes.ADMIN_BROADCAST, R.string.service_group_admin),
            ServiceEntry(Icons.Filled.NotificationsActive, R.string.service_stats, Routes.ADMIN_STATS, R.string.service_group_admin)
        )
    }
}
