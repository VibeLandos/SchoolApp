package com.arzabc.school.ui.components

import android.net.Uri
import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
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
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.ui.window.Dialog
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.MAX_HOMEWORK_PHOTOS
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditHomeworkSheet(
    date: LocalDate,
    period: Int,
    lesson: Lesson?,
    existing: Homework?,
    existingPhotos: List<HomeworkPhoto>,
    photoFile: (String) -> File,
    onDismiss: () -> Unit,
    customSubject: String = "",
    onSave: (
        homework: Homework,
        keepFileNames: List<String>,
        newUris: List<Uri>,
    ) -> Unit,
    onDelete: (Homework) -> Unit,
) {
    var description by remember(existing) { mutableStateOf(existing?.description.orEmpty()) }
    var isDone by remember(existing) { mutableStateOf(existing?.isDone ?: false) }
    var subject by remember(lesson, existing, customSubject) {
        mutableStateOf(
            lesson?.subject
                ?: existing?.subject?.takeIf { it.isNotBlank() }
                ?: customSubject,
        )
    }
    var drafts by remember(existing?.id) {
        mutableStateOf(existingPhotos.map { PhotoDraft.Saved(it.fileName) as PhotoDraft })
    }
    var preview by remember { mutableStateOf<PhotoDraft?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val view = LocalView.current
    var isKeyboardOpen by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val insets = ViewCompat.getRootWindowInsets(view)
            val imeByInsets = insets?.isVisible(WindowInsetsCompat.Type.ime()) == true

            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            val imeByFrame = keypadHeight > screenHeight * 0.15

            isKeyboardOpen = imeByInsets || imeByFrame
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    var isDescFocused by remember { mutableStateOf(false) }
    val isImeActive = isKeyboardOpen || isDescFocused

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
            isDescFocused = false
        }
    }

    BackHandler(enabled = isImeActive) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

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
        onDismissRequest = {
            if (isImeActive) {
                focusManager.clearFocus()
                keyboardController?.hide()
            } else {
                onDismiss()
            }
        },
        properties = ModalBottomSheetDefaults.properties(
            shouldDismissOnBackPress = !isImeActive,
        ),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                },
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
            Text(stringResource(R.string.homework_sheet_title), fontSize = 22.sp)
            val lessonLine = when {
                lesson != null -> stringResource(R.string.homework_sheet_subtitle, period, lesson.subject)
                subject.isNotBlank() -> subject
                else -> stringResource(R.string.lesson_n, period)
            }
            val dateLine = stringResource(
                R.string.date_with_weekday,
                Dates.weekdayName(date),
                Dates.formatFull(date),
            )
            Text(
                text = "$lessonLine\n$dateLine",
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            if (lesson == null) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text(stringResource(R.string.label_subject)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.label_assignment)) },
                placeholder = { Text(stringResource(R.string.hint_assignment)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .onFocusChanged { isDescFocused = it.isFocused },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.photos_count_header, drafts.size, MAX_HOMEWORK_PHOTOS))
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
                        Text(stringResource(R.string.add_photo), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDone, onCheckedChange = { isDone = it })
                Text(stringResource(R.string.done))
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
                            subject = if (lesson == null) subject.trim() else "",
                        ),
                        drafts.filterIsInstance<PhotoDraft.Saved>().map { it.fileName },
                        drafts.filterIsInstance<PhotoDraft.Picked>().map { it.uri },
                    )
                    onDismiss()
                },
                enabled = description.isNotBlank() || drafts.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_homework))
            }
            if (existing != null) {
                TextButton(
                    onClick = {
                        onDelete(existing)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.erase_homework))
                }
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
