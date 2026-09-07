package com.arzabc.school.ui.homework

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.subjectFor
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.time.LocalDate

private enum class TaskFilter { OPEN, DONE, ALL }

@Composable
fun HomeworkListScreen(
    lessons: List<Lesson>,
    homework: List<Homework>,
    photos: List<HomeworkPhoto>,
    onOpenDate: (LocalDate) -> Unit,
    onToggle: (Homework) -> Unit,
    onOpenMenu: () -> Unit,
) {
    var filter by remember { mutableStateOf(TaskFilter.OPEN) }
    val today = remember { LocalDate.now().toEpochDay() }
    val openCount = remember(homework) { homework.count { !it.isDone } }
    val visible = remember(homework, filter) {
        homework.filter {
            when (filter) {
                TaskFilter.OPEN -> !it.isDone
                TaskFilter.DONE -> it.isDone
                TaskFilter.ALL -> true
            }
        }.sortedWith(compareBy<Homework> { it.isDone }.thenBy { it.epochDay }.thenBy { it.period })
    }
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val muted = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.tasks_title),
            subtitle = stringResource(R.string.filter_open) + " · $openCount",
            onOpenMenu = onOpenMenu,
        )
        Row(
            Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filter == TaskFilter.OPEN,
                onClick = { filter = TaskFilter.OPEN },
                label = { Text(stringResource(R.string.filter_open)) },
            )
            FilterChip(
                selected = filter == TaskFilter.DONE,
                onClick = { filter = TaskFilter.DONE },
                label = { Text(stringResource(R.string.filter_done)) },
            )
            FilterChip(
                selected = filter == TaskFilter.ALL,
                onClick = { filter = TaskFilter.ALL },
                label = { Text(stringResource(R.string.filter_all)) },
            )
        }
        if (visible.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.tasks_empty),
                    color = muted,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visible, key = { it.id }) { item ->
                    val date = LocalDate.ofEpochDay(item.epochDay)
                    val overdue = !item.isDone && item.epochDay < today
                    val photoCount = photos.count { it.homeworkId == item.id }
                    val subject = subjectFor(item, lessons)
                    val title = if (subject != null) {
                        stringResource(R.string.task_subject_period, subject, item.period)
                    } else {
                        stringResource(R.string.lesson_n, item.period)
                    }
                    val dateLine = buildString {
                        append(Dates.weekdayName(date))
                        append(", ")
                        append(Dates.formatFull(date))
                        if (overdue) {
                            append(" · ")
                            append(stringResource(R.string.overdue))
                        }
                    }
                    DiaryCard(onClick = { onOpenDate(date) }) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Checkbox(
                                checked = item.isDone,
                                onCheckedChange = { onToggle(item) },
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(top = 10.dp, end = 8.dp),
                            ) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                                    Text(text = Dates.formatDayMonth(date), color = muted, fontSize = 11.sp)
                                }
                                if (item.description.isNotBlank()) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (item.isDone) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        },
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                if (photoCount > 0) {
                                    Text(
                                        text = pluralStringResource(R.plurals.photo_count, photoCount, photoCount),
                                        color = muted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                Text(
                                    text = dateLine,
                                    color = if (overdue) MaterialTheme.colorScheme.error else muted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
