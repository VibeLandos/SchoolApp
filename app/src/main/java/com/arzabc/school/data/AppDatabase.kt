package com.arzabc.school.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Lesson::class, Homework::class, HomeworkPhoto::class, Grade::class],
    version = 4,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun homeworkPhotoDao(): HomeworkPhotoDao
    abstract fun gradeDao(): GradeDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS homework_photos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        homeworkId INTEGER NOT NULL,
                        fileName TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        FOREIGN KEY(homeworkId) REFERENCES homework(id) ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_homework_photos_homeworkId ON homework_photos (homeworkId)",
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE homework ADD COLUMN subject TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS grades (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subject TEXT NOT NULL,
                        value INTEGER NOT NULL,
                        epochDay INTEGER NOT NULL,
                        period INTEGER NOT NULL DEFAULT 0,
                        comment TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_grades_subject_epochDay ON grades (subject, epochDay)",
                )
            }
        }

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "diary.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
    }
}
