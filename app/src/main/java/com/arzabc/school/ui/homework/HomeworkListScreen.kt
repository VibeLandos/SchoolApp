package com.arzabc.school.ui.homework

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.subjectFor
import com.arzabc.school.R
import com.arzabc.school.ui.theme.LocalDiaryPalette
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
    val palette = LocalDiaryPalette.current
    var filter by remember { mutableStateOf(TaskFilter.OPEN) }
    val today = remember { LocalDate.now().toEpochDay() }

    val visible = remember(homework, filter) {
        homework.filter {
            when (filter) {
                TaskFilter.OPEN -> !it.isDone
                TaskFilter.DONE -> it.isDone
                TaskFilter.ALL -> true
            }
        }.sortedWith(compareBy<Homework> { it.isDone }.thenBy { it.epochDay }.thenBy { it.period })
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenMenu) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.cd_open_menu),
                    tint = palette.gold,
                )
            }
            Text(
                text = stringResource(R.string.tasks_title),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 20.dp, top = 12.dp, bottom = 12.dp),
            )
        }
        Row(
            Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = palette.cover,
                labelColor = palette.gold,
                selectedContainerColor = palette.gold,
                selectedLabelColor = palette.onGold,
            )
            FilterChip(
                selected = filter == TaskFilter.OPEN,
                onClick = { filter = TaskFilter.OPEN },
                label = { Text(stringResource(R.string.filter_open)) },
                colors = chipColors,
            )
            FilterChip(
                selected = filter == TaskFilter.DONE,
                onClick = { filter = TaskFilter.DONE },
                label = { Text(stringResource(R.string.filter_done)) },
                colors = chipColors,
            )
            FilterChip(
                selected = filter == TaskFilter.ALL,
                onClick = { filter = TaskFilter.ALL },
                label = { Text(stringResource(R.string.filter_all)) },
                colors = chipColors,
            )
        }

        if (visible.isEmpty()) {
            Text(
                text = stringResource(R.string.tasks_empty),
                color = palette.gold.copy(alpha = 0.8f),
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(20.dp),
            )
        } else {
            LazyColumn(
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
                    val overdueLabel = stringResource(R.string.overdue)
                    val dateLine = buildString {
                        append(Dates.weekdayName(date))
                        append(", ")
                        append(Dates.formatFull(date))
                        if (overdue) {
                            append(" · ")
                            append(overdueLabel)
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDate(date) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = item.isDone,
                            onCheckedChange = { onToggle(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = palette.gold,
                                uncheckedColor = palette.gold.copy(alpha = 0.7f),
                                checkmarkColor = palette.onGold,
                            ),
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = title,
                                color = palette.gold,
                                fontWeight = FontWeight.Medium,
                            )
                            if (item.description.isNotBlank()) {
                                Text(
                                    text = item.description,
                                    color = if (item.isDone) palette.gold.copy(alpha = 0.6f) else palette.paper,
                                    fontFamily = FontFamily.Serif,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                )
                            }
                            if (photoCount > 0) {
                                Text(
                                    text = pluralStringResource(R.plurals.photo_count, photoCount, photoCount),
                                    color = palette.gold.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                text = dateLine,
                                color = if (overdue) palette.homeworkInk else palette.faintInk,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
