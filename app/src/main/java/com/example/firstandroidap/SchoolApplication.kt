package com.example.firstandroidap

import android.app.Application
import com.example.firstandroidap.data.AppDatabase
import com.example.firstandroidap.data.HomeworkPhotoStore
import com.example.firstandroidap.data.SchoolRepository
import com.example.firstandroidap.data.ThemeSettings

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
