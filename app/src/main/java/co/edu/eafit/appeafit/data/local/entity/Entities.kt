package co.edu.eafit.appeafit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val fullName: String,
    val role: String,
    val program: String,
    val institutionalId: String,
    val photoUrl: String,
    val active: Boolean,
    val createdAt: Long
)

@Entity(tableName = "cached_course")
data class CachedCourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val code: String,
    val professorId: String,
    val professorName: String,
    val credits: Int,
    val scheduleJson: String
)

@Entity(tableName = "cached_enrollment", primaryKeys = ["studentId", "courseId"])
data class CachedEnrollmentEntity(
    val studentId: String,
    val courseId: String
)

@Entity(tableName = "cached_grade")
data class CachedGradeEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val courseId: String,
    val courseName: String,
    val item: String,
    val score: Double,
    val maxScore: Double,
    val weightPercent: Double,
    val date: Long
)

@Entity(tableName = "cached_news")
data class CachedNewsEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val body: String,
    val imageUrl: String,
    val authorId: String,
    val publishedAt: Long
)

@Entity(tableName = "cached_calendar_event")
data class CachedCalendarEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val date: Long
)

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val key: String,
    val lastSyncedAt: Long
)
