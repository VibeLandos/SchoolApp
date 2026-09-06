package com.arzabc.school.ui.components

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arzabc.school.R
import com.arzabc.school.ui.theme.CoverDeep
import com.arzabc.school.ui.theme.Paper
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface PhotoDraft {
    data class Saved(val fileName: String) : PhotoDraft
    data class Picked(val uri: Uri) : PhotoDraft
}

fun decodeThumb(file: File, maxPx: Int): ImageBitmap? {
    if (!file.exists()) return null
    val key = "${file.absolutePath}:$maxPx"
    thumbCache.get(key)?.let { return it }
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    val opts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxPx)
        inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
    }
    val bitmap = BitmapFactory.decodeFile(file.absolutePath, opts)?.asImageBitmap()
    if (bitmap != null) thumbCache.put(key, bitmap)
    return bitmap
}

private val thumbCache = object : android.util.LruCache<String, ImageBitmap>(32) {}

fun decodeThumb(resolver: ContentResolver, uri: Uri, maxPx: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val opts = BitmapFactory.Options().apply {
        inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxPx)
    }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        ?.asImageBitmap()
}

private fun sampleSize(width: Int, height: Int, maxPx: Int): Int {
    var size = 1
    while (width / size > maxPx * 2 || height / size > maxPx * 2) {
        size *= 2
    }
    return size.coerceAtLeast(1)
}

@Composable
fun rememberDecodedThumb(
    draft: PhotoDraft,
    photoFile: (String) -> File,
    maxPx: Int,
): ImageBitmap? {
    val resolver = LocalContext.current.contentResolver
    val bitmap by produceState<ImageBitmap?>(initialValue = null, draft, maxPx) {
        value = withContext(Dispatchers.IO) {
            when (draft) {
                is PhotoDraft.Saved -> decodeThumb(photoFile(draft.fileName), maxPx)
                is PhotoDraft.Picked -> decodeThumb(resolver, draft.uri, maxPx)
            }
        }
    }
    return bitmap
}

@Composable
fun HomeworkPhotoThumb(
    draft: PhotoDraft,
    photoFile: (String) -> File,
    onClick: () -> Unit,
    onRemove: (() -> Unit)? = null,
    maxPx: Int = 256,
    modifier: Modifier = Modifier.size(72.dp),
) {
    val bitmap = rememberDecodedThumb(draft, photoFile, maxPx = maxPx)
    Box(modifier = modifier) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = stringResource(R.string.cd_homework_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick),
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Paper.copy(alpha = 0.3f))
                    .clickable(onClick = onClick),
            )
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .background(CoverDeep.copy(alpha = 0.7f), CircleShape),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.cd_remove_photo),
                    tint = Paper,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
