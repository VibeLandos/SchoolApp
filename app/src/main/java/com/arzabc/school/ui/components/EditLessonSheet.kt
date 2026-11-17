package com.arzabc.school.ui.components

import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arzabc.school.R
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SchoolCatalog
import com.arzabc.school.data.SubjectIcon
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditLessonSheet(
    date: LocalDate,
    period: Int,
    existing: Lesson?,
    usedPeriods: Set<Int>,
    bells: BellSchedule,
    onDismiss: () -> Unit,
    onSave: (Lesson) -> Unit,
    onDelete: (Lesson) -> Unit,
    subjectIcons: Map<String, String> = emptyMap(),
    onSetSubjectIcon: ((subject: String, iconKey: String) -> Unit)? = null,
) {
    var subject by remember(existing) { mutableStateOf(existing?.subject.orEmpty()) }
    var room by remember(existing) { mutableStateOf(existing?.room.orEmpty()) }
    var chosenPeriod by remember(existing, period) { mutableStateOf(period) }
    var showIconPicker by remember { mutableStateOf(false) }
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

    var isSubjectFocused by remember { mutableStateOf(false) }
    var isRoomFocused by remember { mutableStateOf(false) }
    val isImeActive = isKeyboardOpen || isSubjectFocused || isRoomFocused

    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) {
            focusManager.clearFocus()
            isSubjectFocused = false
            isRoomFocused = false
        }
    }

    BackHandler(enabled = isImeActive) {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val slot = bells.of(chosenPeriod)
    val otherUsedPeriods = remember(usedPeriods, existing) {
        if (existing != null) usedPeriods - existing.period else usedPeriods
    }
    val periodChoices = remember(otherUsedPeriods, chosenPeriod) {
        val maxRegular = maxOf(SchoolCatalog.PERIODS, chosenPeriod, otherUsedPeriods.maxOrNull() ?: 0)
        (0..maxRegular).toList()
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
            Text(
                text = if (existing == null) {
                    stringResource(R.string.lesson_title, chosenPeriod)
                } else {
                    stringResource(R.string.lesson_title_edit, chosenPeriod)
                },
                fontSize = 22.sp,
            )
            Text(
                text = stringResource(
                    R.string.date_with_weekday,
                    Dates.weekdayName(date),
                    Dates.formatFull(date),
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
            Text(
                text = "${slot.start}–${slot.end}",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = stringResource(R.string.label_period),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 5,
            ) {
                periodChoices.forEach { number ->
                    val taken = number != chosenPeriod && number in otherUsedPeriods
                    FilterChip(
                        selected = chosenPeriod == number,
                        onClick = { if (!taken) chosenPeriod = number },
                        enabled = !taken,
                        label = { Text(number.toString()) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(stringResource(R.string.label_subject)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isSubjectFocused = it.isFocused },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                leadingIcon = {
                    val icon = remember(subject, subjectIcons) {
                        SubjectIcon.resolve(subject, subjectIcons)
                    }
                    IconButton(onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (subject.isNotBlank()) {
                            showIconPicker = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = icon.iconRes),
                            contentDescription = "Значок предмета",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
            Spacer(Modifier.height(8.dp))
            val subjects = stringArrayResource(R.array.subjects)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                subjects.forEach { name ->
                    FilterChip(
                        selected = subject == name,
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            subject = name
                        },
                        label = { Text(name) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.label_room)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isRoomFocused = it.isFocused },
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            val canSave = subject.isNotBlank()
            Button(
                onClick = {
                    onSave(
                        Lesson(
                            id = existing?.id ?: 0,
                            dayOfWeek = Dates.schoolDayOfWeek(date),
                            period = chosenPeriod,
                            subject = subject.trim(),
                            startTime = slot.start,
                            endTime = slot.end,
                            room = room.trim(),
                        ),
                    )
                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Text(stringResource(R.string.save))
            }
            if (!canSave) {
                Text(
                    text = stringResource(R.string.lesson_need_subject),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (existing != null) {
                TextButton(
                    onClick = {
                        onDelete(existing)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.remove_lesson))
                }
            }
        }
    }
}

    if (showIconPicker && subject.isNotBlank()) {
        SubjectIconPickerSheet(
            subject = subject,
            currentIcon = SubjectIcon.resolve(subject, subjectIcons),
            onDismiss = {
                keyboardController?.hide()
                focusManager.clearFocus()
                showIconPicker = false
            },
            onConfirm = { chosenIcon ->
                keyboardController?.hide()
                focusManager.clearFocus()
                onSetSubjectIcon?.invoke(subject, chosenIcon.key)
                showIconPicker = false
            },
        )
    }
}
