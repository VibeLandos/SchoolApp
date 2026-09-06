package com.example.firstandroidap.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

class HomeworkPhotoStore(private val context: Context) {
    val directory: File
        get() = File(context.filesDir, "homework_photos").also { it.mkdirs() }

    fun file(fileName: String): File = File(directory, fileName)

    fun copyFromUri(uri: Uri): String? {
        val name = "${UUID.randomUUID()}.jpg"
        val target = file(name)
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } != null && target.length() > 0L
        }.getOrDefault(false)
        if (!copied) {
            target.delete()
            return null
        }
        return name
    }

    fun delete(fileName: String) {
        file(fileName).delete()
    }
}

const val MAX_HOMEWORK_PHOTOS = 8
