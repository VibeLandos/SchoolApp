package com.arzabc.school.ui.grades

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arzabc.school.R
import com.arzabc.school.data.Grade
import com.arzabc.school.data.Lesson
import com.arzabc.school.ui.components.BlobShape
import com.arzabc.school.ui.components.Md3TopHeader
import java.time.LocalDate
import java.util.Locale

@Composable
fun GradesScreen(
    grades: List<Grade>,
    lessons: List<Lesson>,
    onSaveGrade: (Grade) -> Unit,
    onDeleteGrade: (Grade) -> Unit,
    onOpenProfile: () -> Unit,
) {
    var subjectDetail by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val subjectsWithGrades = remember(grades, lessons) {
        val fromLessons = lessons.map { it.subject }.filter { it.isNotBlank() }.distinct()
        val fromGrades = grades.map { it.subject }.filter { it.isNotBlank() }.distinct()
        (fromGrades + fromLessons).distinct().sorted()
    }

    val overallAverage = remember(grades) {
        if (grades.isEmpty()) null else grades.map { it.value }.average()
    }

    val gradedSubjectsCount = remember(grades) {
        grades.map { it.subject }.distinct().size
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Md3TopHeader(
            subtitle = "Вторая четверть",
            title = "Оценки",
            onAvatarClick = onOpenProfile,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(36.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (overallAverage != null) {
                                String.format(Locale.getDefault(), "%.1f", overallAverage)
                            } else {
                                "—"
                            },
                            fontSize = 72.sp,
                            lineHeight = 76.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Средний балл",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "по $gradedSubjectsCount ${subjectWord(gradedSubjectsCount)}",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            if (subjectsWithGrades.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Оценок пока нет",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Нажмите, чтобы добавить первую оценку",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Добавить оценку")
                        }
                    }
                }
            } else {
                items(subjectsWithGrades, key = { it }) { subject ->
                    val subjectGrades = grades.filter { it.subject == subject }
                    val avg = if (subjectGrades.isEmpty()) null else subjectGrades.map { it.value }.average()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .clickable { subjectDetail = subject }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = subject,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (subjectGrades.isNotEmpty()) {
                                    "${subjectGrades.size} ${gradesWord(subjectGrades.size)}"
                                } else {
                                    "нет оценок"
                                },
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (avg != null) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(BlobShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%.0f", avg),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                        } else {
                            IconButton(onClick = {
                                subjectDetail = subject
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    subjectDetail?.let { subject ->
        val subjectGrades = grades.filter { it.subject == subject }
        SubjectGradeDialog(
            subject = subject,
            grades = subjectGrades,
            onDismiss = { subjectDetail = null },
            onAddGrade = { value ->
                onSaveGrade(
                    Grade(
                        subject = subject,
                        value = value,
                        epochDay = LocalDate.now().toEpochDay(),
                    ),
                )
            },
            onDeleteGrade = onDeleteGrade,
        )
    }

    if (showAddDialog) {
        AddGradePromptDialog(
            availableSubjects = subjectsWithGrades.ifEmpty {
                stringArrayResource(R.array.subjects).toList()
            },
            onDismiss = { showAddDialog = false },
            onAddGrade = { subj, value ->
                onSaveGrade(
                    Grade(
                        subject = subj,
                        value = value,
                        epochDay = LocalDate.now().toEpochDay(),
                    ),
                )
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun SubjectGradeDialog(
    subject: String,
    grades: List<Grade>,
    onDismiss: () -> Unit,
    onAddGrade: (Int) -> Unit,
    onDeleteGrade: (Grade) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = subject,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Поставить оценку:",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 4, 3, 2).forEach { g ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(BlobShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .clickable { onAddGrade(g) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = g.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            if (grades.isNotEmpty()) {
                Text(
                    text = "История оценок:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start),
                )
                Spacer(Modifier.height(6.dp))
                grades.forEach { grade ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Оценка: ${grade.value}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        IconButton(onClick = { onDeleteGrade(grade) }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Закрыть")
            }
        }
    }
}

@Composable
private fun AddGradePromptDialog(
    availableSubjects: List<String>,
    onDismiss: () -> Unit,
    onAddGrade: (String, Int) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(availableSubjects.firstOrNull() ?: "Математика") }
    var selectedGrade by remember { mutableStateOf(5) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Добавить оценку",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            Text("Предмет:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableSubjects.take(3).forEach { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj, maxLines = 1) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Оценка:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 4, 3, 2).forEach { g ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(BlobShape)
                            .background(
                                if (selectedGrade == g) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiaryContainer,
                            )
                            .clickable { selectedGrade = g },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = g.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedGrade == g) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onAddGrade(selectedSubject, selectedGrade) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить")
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Отмена")
            }
        }
    }
}

private fun subjectWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "предметам"
        mod10 == 1 -> "предмету"
        mod10 in 2..4 -> "предметам"
        else -> "предметам"
    }
}

private fun gradesWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "оценок"
        mod10 == 1 -> "оценка"
        mod10 in 2..4 -> "оценки"
        else -> "оценок"
    }
}
