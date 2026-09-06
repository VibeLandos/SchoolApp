package com.example.firstandroidap.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons ORDER BY dayOfWeek, period")
    fun observeAll(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE dayOfWeek = :day AND period = :period LIMIT 1")
    suspend fun find(day: Int, period: Int): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lesson: Lesson)

    @Update
    suspend fun update(lesson: Lesson)

    @Delete
    suspend fun delete(lesson: Lesson)
}

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework ORDER BY epochDay, period")
    fun observeAll(): Flow<List<Homework>>

    @Query("SELECT * FROM homework WHERE epochDay = :epochDay AND period = :period LIMIT 1")
    suspend fun find(epochDay: Long, period: Int): Homework?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(homework: Homework): Long

    @Update
    suspend fun update(homework: Homework)

    @Delete
    suspend fun delete(homework: Homework)
}

@Dao
interface HomeworkPhotoDao {
    @Query("SELECT * FROM homework_photos ORDER BY homeworkId, sortOrder, id")
    fun observeAll(): Flow<List<HomeworkPhoto>>

    @Query("SELECT * FROM homework_photos WHERE homeworkId = :homeworkId ORDER BY sortOrder, id")
    suspend fun forHomework(homeworkId: Long): List<HomeworkPhoto>

    @Insert
    suspend fun insert(photo: HomeworkPhoto)

    @Query("DELETE FROM homework_photos WHERE homeworkId = :homeworkId")
    suspend fun deleteAllFor(homeworkId: Long)

    @Delete
    suspend fun delete(photo: HomeworkPhoto)
}
