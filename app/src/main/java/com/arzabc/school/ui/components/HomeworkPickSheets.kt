package com.arzabc.school.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickTaskSubjectSheet(
    date: LocalDate,
    lessons: List<Lesson>,
    onPickLesson: (Lesson) -> Unit,
    onDismiss: () -> Unit,
) {
    val ordered = remember(lessons) { lessons.sortedBy { it.period } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.pick_task_subject_title),
                fontSize = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                text = stringResource(
                    R.string.date_with_weekday,
                    Dates.weekdayName(date),
                    Dates.formatFull(date),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            if (ordered.isEmpty()) {
                Text(
                    text = stringResource(R.string.pick_task_subject_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    items(ordered, key = { it.id }) { lesson ->
                        DiaryCard(
                            modifier = Modifier.padding(bottom = 10.dp),
                            onClick = { onPickLesson(lesson) },
                        ) {
                            Text(lesson.subject, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stringResource(R.string.lesson_n, lesson.period),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickHomeworkDateSheet(
    subject: String,
    period: Int,
    dates: List<LocalDate>,
    markedEpochDays: Set<Long>,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.pick_hw_date_title),
                fontSize = 22.sp,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Text(
                text = stringResource(R.string.pick_hw_date_subtitle, subject, period),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 12.dp)) {
                items(dates, key = { it.toEpochDay() }) { date ->
                    val has = date.toEpochDay() in markedEpochDays
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(date) }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.date_with_weekday,
                                Dates.weekdayName(date),
                                Dates.formatDayMonth(date),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (has) {
                            Text(
                                text = stringResource(R.string.pick_hw_has_assignment),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
