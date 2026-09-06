package com.example.firstandroidap.data

import android.net.Uri
import java.io.File

class SchoolRepository(
    private val lessonDao: LessonDao,
    private val homeworkDao: HomeworkDao,
    private val photoDao: HomeworkPhotoDao,
    private val photoStore: HomeworkPhotoStore,
) {
    val lessons = lessonDao.observeAll()
    val homework = homeworkDao.observeAll()
    val photos = photoDao.observeAll()

    fun photoFile(fileName: String): File = photoStore.file(fileName)

    suspend fun saveLesson(lesson: Lesson) {
        val existing = if (lesson.id != 0L) {
            lesson
        } else {
            val found = lessonDao.find(lesson.dayOfWeek, lesson.period)
            if (found != null) lesson.copy(id = found.id) else lesson
        }
        if (existing.subject.isBlank()) {
            if (existing.id != 0L) lessonDao.delete(existing)
        } else {
            lessonDao.upsert(existing)
        }
    }

    suspend fun deleteLesson(lesson: Lesson) {
        if (lesson.id != 0L) lessonDao.delete(lesson)
    }

    suspend fun saveHomework(
        homework: Homework,
        keepFileNames: List<String> = emptyList(),
        newUris: List<Uri> = emptyList(),
    ) {
        val existing = if (homework.id != 0L) {
            homework
        } else {
            val found = homeworkDao.find(homework.epochDay, homework.period)
            if (found != null) homework.copy(id = found.id) else homework
        }
        val hasContent = existing.description.isNotBlank() ||
            keepFileNames.isNotEmpty() ||
            newUris.isNotEmpty()
        if (!hasContent) {
            if (existing.id != 0L) deleteHomework(existing)
            return
        }
        val id = if (existing.id != 0L) {
            homeworkDao.upsert(existing)
            existing.id
        } else {
            homeworkDao.upsert(existing)
        }
        replacePhotos(id, keepFileNames, newUris)
    }

    suspend fun deleteHomework(homework: Homework) {
        if (homework.id == 0L) return
        photoDao.forHomework(homework.id).forEach { photoStore.delete(it.fileName) }
        homeworkDao.delete(homework)
    }

    suspend fun toggleHomework(homework: Homework) {
        homeworkDao.update(homework.copy(isDone = !homework.isDone))
    }

    private suspend fun replacePhotos(
        homeworkId: Long,
        keepFileNames: List<String>,
        newUris: List<Uri>,
    ) {
        val current = photoDao.forHomework(homeworkId)
        current.filter { it.fileName !in keepFileNames }.forEach { photoStore.delete(it.fileName) }
        photoDao.deleteAllFor(homeworkId)
        var order = 0
        keepFileNames.take(MAX_HOMEWORK_PHOTOS).forEach { name ->
            photoDao.insert(HomeworkPhoto(homeworkId = homeworkId, fileName = name, sortOrder = order++))
        }
        val remaining = MAX_HOMEWORK_PHOTOS - order
        newUris.take(remaining).forEach { uri ->
            val name = photoStore.copyFromUri(uri) ?: return@forEach
            photoDao.insert(HomeworkPhoto(homeworkId = homeworkId, fileName = name, sortOrder = order++))
        }
    }
}
