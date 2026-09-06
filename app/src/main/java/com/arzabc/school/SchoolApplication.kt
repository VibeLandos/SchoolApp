package com.arzabc.school

import android.app.Application
import com.arzabc.school.data.AppDatabase
import com.arzabc.school.data.HomeworkPhotoStore
import com.arzabc.school.data.SchoolRepository
import com.arzabc.school.data.ThemeSettings

class SchoolApplication : Application() {
    val themeSettings: ThemeSettings by lazy { ThemeSettings(this) }

    val repository: SchoolRepository by lazy {
        val db = AppDatabase.create(this)
        SchoolRepository(
            db.lessonDao(),
            db.homeworkDao(),
            db.homeworkPhotoDao(),
            HomeworkPhotoStore(this),
        )
    }
}
