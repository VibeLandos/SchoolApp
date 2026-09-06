package com.arzabc.school.ui.components

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
import com.arzabc.school.R
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SchoolCatalog
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
) {
    var subject by remember(existing) { mutableStateOf(existing?.subject.orEmpty()) }
    var room by remember(existing) { mutableStateOf(existing?.room.orEmpty()) }
    var chosenPeriod by remember(existing, period) { mutableStateOf(period) }
    val slot = bells.of(chosenPeriod)
    val periodChoices = remember(usedPeriods, chosenPeriod) {
        val maxRegular = maxOf(SchoolCatalog.PERIODS, chosenPeriod, usedPeriods.maxOrNull() ?: 0)
        (0..maxRegular).toList()
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                periodChoices.forEach { number ->
                    val taken = number != chosenPeriod && number in usedPeriods
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
                            period = chosenPeriod,
                            subject = subject.trim(),
                            startTime = slot.start,
                            endTime = slot.end,
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
