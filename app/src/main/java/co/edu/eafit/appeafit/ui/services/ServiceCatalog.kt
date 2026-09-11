package co.edu.eafit.appeafit.ui.services

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.ui.graphics.vector.ImageVector
import co.edu.eafit.appeafit.R
import co.edu.eafit.appeafit.domain.model.Role
import co.edu.eafit.appeafit.ui.navigation.Routes

data class ServiceEntry(
    val icon: ImageVector,
    val labelResId: Int,
    val route: String,
    val group: String
)

object ServiceCatalog {

    fun forRole(role: Role): List<ServiceEntry> = when (role) {
        Role.STUDENT -> listOf(
            ServiceEntry(Icons.Filled.Assessment, R.string.service_grades, Routes.STUDENT_GRADES, "Académico"),
            ServiceEntry(Icons.Filled.CalendarMonth, R.string.service_schedule, Routes.STUDENT_SCHEDULE, "Académico"),
            ServiceEntry(Icons.Filled.Calculate, R.string.service_gpa_calculator, Routes.STUDENT_GPA_CALCULATOR, "Académico"),
            ServiceEntry(Icons.Filled.RateReview, R.string.service_teacher_evaluation, Routes.STUDENT_TEACHER_EVALUATION, "Académico"),
            ServiceEntry(Icons.Filled.EventAvailable, R.string.service_academic_calendar, Routes.STUDENT_ACADEMIC_CALENDAR, "Académico"),
            ServiceEntry(Icons.AutoMirrored.Filled.MenuBook, R.string.service_library, Routes.STUDENT_LIBRARY, "Biblioteca"),
            ServiceEntry(Icons.Filled.MeetingRoom, R.string.service_space_reservation, Routes.STUDENT_SPACE_RESERVATION, "Campus Life"),
            ServiceEntry(Icons.Filled.Search, R.string.service_lost_items, Routes.STUDENT_LOST_ITEMS, "Campus Life")
        )
        Role.PROFESSOR -> listOf(
            ServiceEntry(Icons.Filled.Groups, R.string.service_my_courses, Routes.PROFESSOR_MY_COURSES, "Docencia"),
            ServiceEntry(Icons.Filled.HowToReg, R.string.service_attendance, Routes.PROFESSOR_ATTENDANCE, "Docencia"),
            ServiceEntry(Icons.AutoMirrored.Filled.Assignment, R.string.service_grade_entry, Routes.PROFESSOR_GRADE_ENTRY, "Docencia"),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_announcements, Routes.PROFESSOR_ANNOUNCEMENTS, "Docencia"),
            ServiceEntry(Icons.Filled.MeetingRoom, R.string.service_space_reservation, Routes.STUDENT_SPACE_RESERVATION, "Campus Life")
        )
        Role.STAFF -> listOf(
            ServiceEntry(Icons.Filled.ContactMail, R.string.service_directory, Routes.STAFF_DIRECTORY, "Institucional"),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_manage_announcements, Routes.STAFF_MANAGE_ANNOUNCEMENTS, "Institucional"),
            ServiceEntry(Icons.Filled.Checklist, R.string.service_manage_reservations, Routes.STAFF_MANAGE_RESERVATIONS, "Campus Life"),
            ServiceEntry(Icons.Filled.Search, R.string.service_manage_lost_items, Routes.STAFF_MANAGE_LOST_ITEMS, "Campus Life")
        )
        Role.ADMIN -> listOf(
            ServiceEntry(Icons.Filled.SupervisedUserCircle, R.string.service_manage_users, Routes.ADMIN_MANAGE_USERS, "Administración"),
            ServiceEntry(Icons.Filled.Campaign, R.string.service_manage_content, Routes.ADMIN_MANAGE_CONTENT, "Administración"),
            ServiceEntry(Icons.AutoMirrored.Filled.Send, R.string.service_broadcast, Routes.ADMIN_BROADCAST, "Administración"),
            ServiceEntry(Icons.Filled.NotificationsActive, R.string.service_stats, Routes.ADMIN_STATS, "Administración")
        )
    }
}
