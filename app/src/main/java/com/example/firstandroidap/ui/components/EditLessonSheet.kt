package com.example.firstandroidap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstandroidap.R
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.SchoolCatalog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditLessonSheet(
    date: LocalDate,
    period: Int,
    existing: Lesson?,
    onDismiss: () -> Unit,
    onSave: (Lesson) -> Unit,
    onDelete: (Lesson) -> Unit,
) {
    val bells = SchoolCatalog.bells(period)
    var subject by remember(existing) { mutableStateOf(existing?.subject.orEmpty()) }
    var room by remember(existing) { mutableStateOf(existing?.room.orEmpty()) }

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
            Text(
                text = if (existing == null) {
                    stringResource(R.string.lesson_title, period)
                } else {
                    stringResource(R.string.lesson_title_edit, period)
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
                text = "${bells.first}–${bells.second}",
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text(stringResource(R.string.label_subject)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            Spacer(Modifier.height(8.dp))
            val subjects = stringArrayResource(R.array.subjects)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                subjects.forEach { name ->
                    FilterChip(
                        selected = subject == name,
                        onClick = { subject = name },
                        label = { Text(name) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text(stringResource(R.string.label_room)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        Lesson(
                            id = existing?.id ?: 0,
                            dayOfWeek = Dates.schoolDayOfWeek(date),
                            period = period,
                            subject = subject.trim(),
                            startTime = bells.first,
                            endTime = bells.second,
                            room = room.trim(),
                        ),
                    )
                    onDismiss()
                },
                enabled = subject.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
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
