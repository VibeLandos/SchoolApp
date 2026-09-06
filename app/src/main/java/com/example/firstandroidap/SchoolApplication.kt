package com.example.firstandroidap

import android.app.Application
import com.example.firstandroidap.data.AppDatabase
import com.example.firstandroidap.data.HomeworkPhotoStore
import com.example.firstandroidap.data.SchoolRepository

class SchoolApplication : Application() {
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
