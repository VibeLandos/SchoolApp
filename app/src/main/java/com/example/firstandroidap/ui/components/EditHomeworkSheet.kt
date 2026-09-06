package com.example.firstandroidap.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.HomeworkPhoto
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.MAX_HOMEWORK_PHOTOS
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHomeworkSheet(
    date: LocalDate,
    period: Int,
    lesson: Lesson?,
    existing: Homework?,
    existingPhotos: List<HomeworkPhoto>,
    photoFile: (String) -> File,
    onDismiss: () -> Unit,
    onSave: (
        homework: Homework,
        subjectIfNeeded: String,
        keepFileNames: List<String>,
        newUris: List<Uri>,
    ) -> Unit,
    onDelete: (Homework) -> Unit,
) {
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }
    var isDone by remember(existing) { mutableStateOf(existing?.isDone ?: false) }
    var subject by remember(lesson) { mutableStateOf(lesson?.subject.orEmpty()) }
    var drafts by remember(existing?.id) {
        mutableStateOf(existingPhotos.map { PhotoDraft.Saved(it.fileName) as PhotoDraft })
    }
    var preview by remember { mutableStateOf<PhotoDraft?>(null) }

    fun addUris(uris: List<Uri>) {
        val room = MAX_HOMEWORK_PHOTOS - drafts.size
        if (room <= 0) return
        drafts = drafts + uris.take(room).map { PhotoDraft.Picked(it) }
    }

    val multiPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_HOMEWORK_PHOTOS),
    ) { uris -> addUris(uris) }
    val singlePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) addUris(listOf(uri)) }

    fun pickPhotos() {
        val remaining = MAX_HOMEWORK_PHOTOS - drafts.size
        when {
            remaining <= 0 -> Unit
            remaining == 1 -> singlePicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
            else -> multiPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text("Домашнее задание", fontSize = 22.sp)
            Text(
                text = buildString {
                    append("Урок $period")
                    if (lesson != null) append(" · ${lesson.subject}")
                    append("\n${Dates.weekdayName(date)}, ${Dates.formatFull(date)}")
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            if (lesson == null) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Предмет") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Что задали") },
                placeholder = { Text("упр. 12, стр. 34, §5") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Spacer(Modifier.height(12.dp))
            Text("Фото (${drafts.size}/$MAX_HOMEWORK_PHOTOS)")
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                drafts.forEachIndexed { index, draft ->
                    HomeworkPhotoThumb(
                        draft = draft,
                        photoFile = photoFile,
                        onClick = { preview = draft },
                        onRemove = { drafts = drafts.filterIndexed { i, _ -> i != index } },
                    )
                }
                if (drafts.size < MAX_HOMEWORK_PHOTOS) {
                    OutlinedButton(onClick = ::pickPhotos) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = null)
                        Text("Добавить", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDone, onCheckedChange = { isDone = it })
                Text("Сделано")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onSave(
                        Homework(
                            id = existing?.id ?: 0,
                            epochDay = date.toEpochDay(),
                            period = period,
                            description = description.trim(),
                            isDone = isDone,
                        ),
                        subject.trim(),
                        drafts.filterIsInstance<PhotoDraft.Saved>().map { it.fileName },
                        drafts.filterIsInstance<PhotoDraft.Picked>().map { it.uri },
                    )
                    onDismiss()
                },
                enabled = description.isNotBlank() || drafts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Записать в дневник")
            }
            if (existing != null) {
                TextButton(
                    onClick = {
                        onDelete(existing)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Стереть задание")
                }
            }
        }
    }

    val shown = preview
    if (shown != null) {
        val large = rememberDecodedThumb(shown, photoFile, maxPx = 1600)
        Dialog(onDismissRequest = { preview = null }) {
            if (large != null) {
                Image(
                    bitmap = large,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .clickable { preview = null },
                )
            }
        }
    }
}
