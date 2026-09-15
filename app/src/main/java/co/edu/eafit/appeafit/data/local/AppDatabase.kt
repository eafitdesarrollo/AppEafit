package co.edu.eafit.appeafit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import co.edu.eafit.appeafit.data.local.dao.CalendarEventDao
import co.edu.eafit.appeafit.data.local.dao.CourseDao
import co.edu.eafit.appeafit.data.local.dao.EnrollmentDao
import co.edu.eafit.appeafit.data.local.dao.GradeDao
import co.edu.eafit.appeafit.data.local.dao.NewsDao
import co.edu.eafit.appeafit.data.local.dao.SyncStateDao
import co.edu.eafit.appeafit.data.local.dao.UserDao
import co.edu.eafit.appeafit.data.local.entity.CachedCalendarEventEntity
import co.edu.eafit.appeafit.data.local.entity.CachedCourseEntity
import co.edu.eafit.appeafit.data.local.entity.CachedEnrollmentEntity
import co.edu.eafit.appeafit.data.local.entity.CachedGradeEntity
import co.edu.eafit.appeafit.data.local.entity.CachedNewsEntity
import co.edu.eafit.appeafit.data.local.entity.CachedUserEntity
import co.edu.eafit.appeafit.data.local.entity.SyncStateEntity

@Database(
    entities = [
        CachedUserEntity::class,
        CachedCourseEntity::class,
        CachedEnrollmentEntity::class,
        CachedGradeEntity::class,
        CachedNewsEntity::class,
        CachedCalendarEventEntity::class,
        SyncStateEntity::class
    ],
    // v2 (2026-09-15): CachedGradeEntity agregó el campo `corte` (ver domain/model/Grade.kt).
    // Sin migración explícita: fallbackToDestructiveMigration(true) borra y recrea el
    // caché local completo en el primer arranque tras la actualización (todos los
    // repositorios re-sincronizan solos desde Firestore en su próximo refresh, así que
    // no hay pérdida de datos reales, solo un refresh extra la primera vez).
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun courseDao(): CourseDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun gradeDao(): GradeDao
    abstract fun newsDao(): NewsDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun syncStateDao(): SyncStateDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eafit_offline.db"
                ).fallbackToDestructiveMigration(true).build().also { instance = it }
            }
    }
}
