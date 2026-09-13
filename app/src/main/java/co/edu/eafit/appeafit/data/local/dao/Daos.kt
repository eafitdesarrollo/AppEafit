package co.edu.eafit.appeafit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import co.edu.eafit.appeafit.data.local.entity.CachedCalendarEventEntity
import co.edu.eafit.appeafit.data.local.entity.CachedCourseEntity
import co.edu.eafit.appeafit.data.local.entity.CachedEnrollmentEntity
import co.edu.eafit.appeafit.data.local.entity.CachedGradeEntity
import co.edu.eafit.appeafit.data.local.entity.CachedNewsEntity
import co.edu.eafit.appeafit.data.local.entity.CachedUserEntity
import co.edu.eafit.appeafit.data.local.entity.SyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: CachedUserEntity)

    @Query("SELECT * FROM cached_user WHERE uid = :uid LIMIT 1")
    fun observe(uid: String): Flow<CachedUserEntity?>

    @Query("SELECT * FROM cached_user WHERE uid = :uid LIMIT 1")
    suspend fun get(uid: String): CachedUserEntity?

    @Query("DELETE FROM cached_user")
    suspend fun clear()
}

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(courses: List<CachedCourseEntity>)

    @Query("SELECT * FROM cached_course WHERE id IN (:ids)")
    fun observeByIds(ids: List<String>): Flow<List<CachedCourseEntity>>

    @Query("SELECT * FROM cached_course WHERE professorId = :professorId")
    fun observeByProfessor(professorId: String): Flow<List<CachedCourseEntity>>

    @Query("DELETE FROM cached_course")
    suspend fun clear()

    @Query("DELETE FROM cached_course WHERE professorId = :professorId")
    suspend fun clearForProfessor(professorId: String)
}

@Dao
interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(enrollments: List<CachedEnrollmentEntity>)

    @Query("SELECT courseId FROM cached_enrollment WHERE studentId = :studentId")
    fun observeCourseIds(studentId: String): Flow<List<String>>

    @Query("DELETE FROM cached_enrollment WHERE studentId = :studentId")
    suspend fun clearForStudent(studentId: String)
}

@Dao
interface GradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(grades: List<CachedGradeEntity>)

    @Query("SELECT * FROM cached_grade WHERE studentId = :studentId ORDER BY date DESC")
    fun observeForStudent(studentId: String): Flow<List<CachedGradeEntity>>

    @Query("DELETE FROM cached_grade WHERE studentId = :studentId")
    suspend fun clearForStudent(studentId: String)
}

@Dao
interface NewsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(news: List<CachedNewsEntity>)

    @Query("SELECT * FROM cached_news ORDER BY publishedAt DESC")
    fun observeAll(): Flow<List<CachedNewsEntity>>

    @Query("DELETE FROM cached_news")
    suspend fun clear()
}

@Dao
interface CalendarEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<CachedCalendarEventEntity>)

    @Query("SELECT * FROM cached_calendar_event ORDER BY date ASC")
    fun observeAll(): Flow<List<CachedCalendarEventEntity>>

    @Query("DELETE FROM cached_calendar_event")
    suspend fun clear()
}

@Dao
interface SyncStateDao {
    @Upsert
    suspend fun upsert(state: SyncStateEntity)

    @Query("SELECT * FROM sync_state WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): SyncStateEntity?
}
