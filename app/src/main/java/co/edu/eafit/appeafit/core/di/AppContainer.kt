package co.edu.eafit.appeafit.core.di

import android.content.Context
import co.edu.eafit.appeafit.core.connectivity.ConnectivityObserver
import co.edu.eafit.appeafit.data.local.AppDatabase
import co.edu.eafit.appeafit.data.repository.AttendanceRepository
import co.edu.eafit.appeafit.data.repository.AuthRepository
import co.edu.eafit.appeafit.data.repository.CalendarRepository
import co.edu.eafit.appeafit.data.repository.CourseRepository
import co.edu.eafit.appeafit.data.repository.GradeRepository
import co.edu.eafit.appeafit.data.repository.LoanRepository
import co.edu.eafit.appeafit.data.repository.LostItemRepository
import co.edu.eafit.appeafit.data.repository.NewsRepository
import co.edu.eafit.appeafit.data.repository.NotificationRepository
import co.edu.eafit.appeafit.data.repository.ReservationRepository
import co.edu.eafit.appeafit.data.repository.SettingsRepository
import co.edu.eafit.appeafit.data.repository.TeacherEvaluationRepository
import co.edu.eafit.appeafit.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AppContainer(context: Context) {

    // Firestore already ships its own persistent disk cache. We additionally keep a
    // lightweight Room mirror so grades/schedule/news/calendar render instantly
    // offline without waiting on Firestore's cache warm-up, and survive a full
    // reinstall-free cold start with zero network.
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val database: AppDatabase = AppDatabase.getInstance(context)

    val connectivityObserver = ConnectivityObserver(context)

    val authRepository = AuthRepository(auth)
    val userRepository = UserRepository(firestore, database.userDao())
    val newsRepository = NewsRepository(firestore, database.newsDao())
    val courseRepository = CourseRepository(firestore, database.courseDao(), database.enrollmentDao())
    val gradeRepository = GradeRepository(firestore, database.gradeDao())
    val calendarRepository = CalendarRepository(firestore, database.calendarEventDao())
    val lostItemRepository = LostItemRepository(firestore)
    val loanRepository = LoanRepository(firestore)
    val reservationRepository = ReservationRepository(firestore)
    val notificationRepository = NotificationRepository(firestore)
    val attendanceRepository = AttendanceRepository(firestore)
    val settingsRepository = SettingsRepository(context.applicationContext)
    val teacherEvaluationRepository = TeacherEvaluationRepository(firestore)
}
