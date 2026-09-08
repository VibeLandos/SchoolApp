package com.arzabc.school.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Lesson::class, Homework::class, HomeworkPhoto::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun homeworkPhotoDao(): HomeworkPhotoDao

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

        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "diary.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
