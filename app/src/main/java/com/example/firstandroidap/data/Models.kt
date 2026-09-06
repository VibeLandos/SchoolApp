package com.example.firstandroidap.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lessons",
    indices = [Index(value = ["dayOfWeek", "period"], unique = true)],
)
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int,
    val period: Int,
    val subject: String,
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
)

@Entity(
    tableName = "homework",
    indices = [Index(value = ["epochDay", "period"], unique = true)],
)
data class Homework(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val period: Int,
    val description: String,
    val isDone: Boolean = false,
)

@Entity(
    tableName = "homework_photos",
    foreignKeys = [
        ForeignKey(
            entity = Homework::class,
            parentColumns = ["id"],
            childColumns = ["homeworkId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("homeworkId")],
)
data class HomeworkPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val homeworkId: Long,
    val fileName: String,
    val sortOrder: Int = 0,
)
