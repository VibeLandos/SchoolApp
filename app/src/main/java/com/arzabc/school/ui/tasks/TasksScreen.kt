package com.arzabc.school.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SubjectIcon
import com.arzabc.school.ui.components.Md3TopHeader
import com.arzabc.school.ui.components.SubjectIconBadge
import com.arzabc.school.ui.components.SubjectIconPickerSheet
import java.time.LocalDate

enum class TasksFilter {
    All,
    Today,
    Overdue,
}

@Composable
fun TasksScreen(
    homework: List<Homework>,
    lessons: List<Lesson>,
    onToggleHomework: (Homework) -> Unit,
    onOpenHomework: (Homework) -> Unit,
    onAddHomework: () -> Unit,
    onOpenProfile: () -> Unit,
    subjectIcons: Map<String, String> = emptyMap(),
    onSetSubjectIcon: (subject: String, iconKey: String) -> Unit = { _, _ -> },
) {
    var filter by remember { mutableStateOf(TasksFilter.All) }
    var iconPickerSubject by remember { mutableStateOf<String?>(null) }
    val today = remember { LocalDate.now() }
    val todayEpoch = today.toEpochDay()

    val filteredHomework = remember(homework, filter, todayEpoch) {
        when (filter) {
            TasksFilter.All -> homework.sortedBy { it.epochDay }
            TasksFilter.Today -> homework.filter { it.epochDay == todayEpoch }.sortedBy { it.period }
            TasksFilter.Overdue -> homework.filter { it.epochDay < todayEpoch && !it.isDone }.sortedBy { it.epochDay }
        }
    }

    val totalCount = homework.size
    val doneCount = remember(homework) { homework.count { it.isDone } }
    val remainingCount = totalCount - doneCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Md3TopHeader(
                subtitle = "Осталось $remainingCount ${taskWord(remainingCount)}",
                title = "Задания",
                onAvatarClick = onOpenProfile,
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = filter == TasksFilter.All,
                    onClick = { filter = TasksFilter.All },
                    label = { Text("Все", fontSize = 15.sp, fontWeight = if (filter == TasksFilter.All) FontWeight.Medium else FontWeight.Normal) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
                FilterChip(
                    selected = filter == TasksFilter.Today,
                    onClick = { filter = TasksFilter.Today },
                    label = { Text("Сегодня", fontSize = 15.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
                FilterChip(
                    selected = filter == TasksFilter.Overdue,
                    onClick = { filter = TasksFilter.Overdue },
                    label = { Text("Просрочено", fontSize = 15.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Progress Card
                item {
                    val progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(32.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                    ) {
                        Column {
                            Text(
                                text = "Выполнено $doneCount из $totalCount",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                if (filteredHomework.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = when (filter) {
                                    TasksFilter.All -> "Заданий пока нет"
                                    TasksFilter.Today -> "На сегодня заданий нет"
                                    TasksFilter.Overdue -> "Просроченных заданий нет"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    items(filteredHomework, key = { it.id }) { item ->
                        val date = LocalDate.ofEpochDay(item.epochDay)
                        val isOverdue = item.epochDay < todayEpoch && !item.isDone
                        val dateLabel = when {
                            item.epochDay == todayEpoch -> "сегодня"
                            item.epochDay == todayEpoch + 1 -> "до завтра"
                            isOverdue -> "просрочено"
                            else -> Dates.formatDayMonth(date)
                        }
                        val subjectName = item.subject.ifBlank {
                            lessons.firstOrNull { it.period == item.period }?.subject ?: "Урок ${item.period}"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { onOpenHomework(item) }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Circular Checkbox
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (item.isDone) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    )
                                    .border(
                                        width = if (item.isDone) 0.dp else 2.dp,
                                        color = if (item.isDone) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape,
                                    )
                                    .clickable { onToggleHomework(item) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (item.isDone) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }

                            Spacer(Modifier.width(14.dp))

                            SubjectIconBadge(
                                icon = SubjectIcon.resolve(subjectName, subjectIcons),
                                size = 44.dp,
                                iconSize = 24.dp,
                                shape = RoundedCornerShape(14.dp),
                                onClick = { iconPickerSubject = subjectName },
                            )

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.description.ifBlank { "Без описания" },
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.isDone) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "$subjectName · $dateLabel",
                                    fontSize = 15.sp,
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Extended Floating Action Button
        ExtendedFloatingActionButton(
            onClick = onAddHomework,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Добавить", fontSize = 16.sp, fontWeight = FontWeight.Medium) },
        )
    }

    iconPickerSubject?.let { subj ->
        SubjectIconPickerSheet(
            subject = subj,
            currentIcon = SubjectIcon.resolve(subj, subjectIcons),
            onDismiss = { iconPickerSubject = null },
            onConfirm = { icon ->
                onSetSubjectIcon(subj, icon.key)
                iconPickerSubject = null
            },
        )
    }
}

private fun taskWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "заданий"
        mod10 == 1 -> "задание"
        mod10 in 2..4 -> "задания"
        else -> "заданий"
    }
}
