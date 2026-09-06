package com.example.firstandroidap.ui.homework

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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.HomeworkPhoto
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.subjectFor
import com.example.firstandroidap.R
import com.example.firstandroidap.ui.theme.LocalDiaryPalette
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
                text = "Задания",
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
            FilterChip(
                selected = filter == TaskFilter.OPEN,
                onClick = { filter = TaskFilter.OPEN },
                label = { Text("Не сделано") },
            )
            FilterChip(
                selected = filter == TaskFilter.DONE,
                onClick = { filter = TaskFilter.DONE },
                label = { Text("Сделано") },
            )
            FilterChip(
                selected = filter == TaskFilter.ALL,
                onClick = { filter = TaskFilter.ALL },
                label = { Text("Все") },
            )
        }

        if (visible.isEmpty()) {
            Text(
                text = "Пока пусто — запиши д/з на странице дневника, в клетке справа.",
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
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${subjectFor(item, lessons)} · урок ${item.period}",
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
                                    text = "$photoCount фото",
                                    color = palette.gold.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                )
                            }
                            Text(
                                text = buildString {
                                    append(Dates.weekdayName(date))
                                    append(", ")
                                    append(Dates.formatFull(date))
                                    if (overdue) append(" · просрочено")
                                },
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
