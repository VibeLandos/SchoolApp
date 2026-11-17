package com.arzabc.school.ui.diary

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arzabc.school.R
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Grade
import com.arzabc.school.data.Homework
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.NowStatus
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.nowStatus
import com.arzabc.school.ui.components.BlobShape
import com.arzabc.school.ui.components.HeroBannerCard
import com.arzabc.school.ui.components.Md3LessonCard
import com.arzabc.school.ui.components.Md3TopHeader
import com.arzabc.school.ui.components.SubjectIconPickerSheet
import com.arzabc.school.data.SubjectIcon
import androidx.compose.ui.res.painterResource
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    lessons: List<Lesson>,
    homework: List<Homework>,
    grades: List<Grade>,
    weekBells: WeekBells,
    schoolDays: Int,
    onSelectDate: (LocalDate) -> Unit,
    onEditLesson: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onDeleteLesson: (Lesson) -> Unit,
    onAddHomework: (lesson: Lesson, date: LocalDate) -> Unit,
    onOpenHomework: (Homework) -> Unit,
    onSaveGrade: (Grade) -> Unit,
    onDeleteGrade: (Grade) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
    onOpenProfile: () -> Unit,
    subjectIcons: Map<String, String> = emptyMap(),
    onSetSubjectIcon: (subject: String, iconKey: String) -> Unit = { _, _ -> },
) {
    val lessonsByDay = remember(lessons) { lessons.groupBy { it.dayOfWeek } }
    var clock by remember { mutableStateOf(LocalTime.now() to LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now() to LocalDate.now()
            delay(15_000)
        }
    }

    val (time, today) = clock
    val todayStatus = remember(clock, lessons, weekBells, schoolDays) {
        nowStatus(today, time, lessons, weekBells.forDay(today.dayOfWeek.value), schoolDays)
    }

    val startPage = Dates.dayIndex(selectedDate, schoolDays)
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { weekDates.size.coerceAtLeast(1) },
    )
    val pagerScope = rememberCoroutineScope()
    val weekDatesState = rememberUpdatedState(weekDates)
    val onSelectDateState = rememberUpdatedState(onSelectDate)

    LaunchedEffect(selectedDate) {
        val target = Dates.dayIndex(selectedDate, schoolDays)
        if (pagerState.currentPage != target && !pagerState.isScrollInProgress) {
            pagerState.scrollToPage(target)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val date = weekDatesState.value.getOrNull(page) ?: return@collect
                onSelectDateState.value(date)
            }
    }

    val headerSubtitle = remember(selectedDate) {
        try {
            val pattern = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("ru"))
            selectedDate.format(pattern).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
        } catch (_: Exception) {
            Dates.weekdayName(selectedDate) + ", " + Dates.formatDayMonth(selectedDate)
        }
    }

    var activeLessonForMenu by remember { mutableStateOf<Pair<Lesson, LocalDate>?>(null) }
    var activeGradePickerLesson by remember { mutableStateOf<Triple<Lesson, LocalDate, Grade?>?>(null) }
    var activeIconPickerSubject by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            Md3TopHeader(
                subtitle = headerSubtitle,
                title = "Дневник",
                onAvatarClick = onOpenProfile,
            )

            // Day chips
            Md3WeekDayChips(
                selectedDate = selectedDate,
                weekDates = weekDates,
                today = today,
                onSelectDate = { date ->
                    onSelectDate(date)
                    val target = Dates.dayIndex(date, schoolDays)
                    pagerScope.launch { pagerState.animateScrollToPage(target) }
                },
            )

            Spacer(Modifier.height(4.dp))

            // Swipeable Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                beyondBoundsPageCount = 0,
            ) { page ->
                val date = weekDates.getOrNull(page) ?: selectedDate
                DiaryDayPage(
                    date = date,
                    today = today,
                    lessons = lessonsByDay[Dates.schoolDayOfWeek(date)].orEmpty(),
                    homework = homework,
                    grades = grades,
                    bells = weekBells.forDay(Dates.schoolDayOfWeek(date)),
                    todayStatus = if (date == today) todayStatus else null,
                    subjectIcons = subjectIcons,
                    onPickIcon = { s -> activeIconPickerSubject = s },
                    onLessonClick = { lesson -> activeLessonForMenu = lesson to date },
                    onAddLesson = { onAddLesson(date) },
                )
            }
        }

        // Floating Action Button to add lesson
        FloatingActionButton(
            onClick = { onAddLesson(selectedDate) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(18.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_lesson))
        }
    }

    // Action dialog when a lesson is clicked
    activeLessonForMenu?.let { (lesson, date) ->
        val existingGrade = grades.firstOrNull {
            it.subject.equals(lesson.subject, ignoreCase = true) && it.epochDay == date.toEpochDay()
        }
        val existingHw = homework.firstOrNull {
            it.epochDay == date.toEpochDay() && it.period == lesson.period
        }

        LessonActionDialog(
            lesson = lesson,
            date = date,
            existingGrade = existingGrade,
            existingHomework = existingHw,
            subjectIcons = subjectIcons,
            onDismiss = { activeLessonForMenu = null },
            onPickIcon = {
                activeLessonForMenu = null
                activeIconPickerSubject = lesson.subject
            },
            onOpenHomework = {
                activeLessonForMenu = null
                if (existingHw != null) {
                    onOpenHomework(existingHw)
                } else {
                    onAddHomework(lesson, date)
                }
            },
            onPickGrade = {
                activeLessonForMenu = null
                activeGradePickerLesson = Triple(lesson, date, existingGrade)
            },
            onEditLesson = {
                activeLessonForMenu = null
                onEditLesson(date, lesson.period, lesson)
            },
            onDeleteLesson = {
                activeLessonForMenu = null
                onDeleteLesson(lesson)
            },
        )
    }

    // Quick grade picker dialog
    activeGradePickerLesson?.let { (lesson, date, existingGrade) ->
        QuickGradePickerDialog(
            subject = lesson.subject,
            existingGrade = existingGrade,
            onDismiss = { activeGradePickerLesson = null },
            onSelectGrade = { value ->
                activeGradePickerLesson = null
                onSaveGrade(
                    Grade(
                        id = existingGrade?.id ?: 0,
                        subject = lesson.subject,
                        value = value,
                        epochDay = date.toEpochDay(),
                        comment = "",
                    ),
                )
            },
            onDeleteGrade = {
                activeGradePickerLesson = null
                if (existingGrade != null) {
                    onDeleteGrade(existingGrade)
                }
            },
        )
    }

    // Subject icon picker bottom sheet
    activeIconPickerSubject?.let { subject ->
        SubjectIconPickerSheet(
            subject = subject,
            currentIcon = SubjectIcon.resolve(subject, subjectIcons),
            onDismiss = { activeIconPickerSubject = null },
            onConfirm = { newIcon ->
                onSetSubjectIcon(subject, newIcon.key)
                activeIconPickerSubject = null
            },
        )
    }
}

@Composable
private fun Md3WeekDayChips(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        weekDates.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            val shape = RoundedCornerShape(16.dp)

            val bg = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }

            val fg = if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(shape)
                    .background(bg)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = shape,
                    )
                    .clickable { onSelectDate(date) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = Dates.weekdayShort(date).uppercase(),
                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else fg,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else fg,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun DiaryDayPage(
    date: LocalDate,
    today: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    grades: List<Grade>,
    bells: BellSchedule,
    todayStatus: NowStatus?,
    subjectIcons: Map<String, String>,
    onPickIcon: (String) -> Unit,
    onLessonClick: (Lesson) -> Unit,
    onAddLesson: () -> Unit,
) {
    val ordered = remember(lessons) { lessons.sortedBy { it.period } }
    val count = ordered.size
    val isToday = date == today

    val heroTitle = when {
        isToday && count == 0 -> "Сегодня уроков нет"
        isToday -> "Сегодня $count ${lessonWord(count)}"
        count == 0 -> "Уроков нет"
        else -> "${Dates.weekdayName(date)}: $count ${lessonWord(count)}"
    }

    val heroSubtitle = when {
        isToday && todayStatus is NowStatus.InLesson -> {
            val curr = todayStatus.current
            val roomStr = if (curr.room.isNotBlank()) " (${curr.room})" else ""
            "Сейчас идет ${curr.subject}$roomStr"
        }
        isToday && todayStatus is NowStatus.InBreak -> {
            "Перемена · следующий: ${todayStatus.next.subject}"
        }
        isToday && todayStatus is NowStatus.Before && ordered.isNotEmpty() -> {
            "Первый урок: ${todayStatus.first.subject} через ${todayStatus.minutes} мин"
        }
        isToday && todayStatus is NowStatus.After -> {
            val tomorrowHw = homework.firstOrNull { it.epochDay == date.toEpochDay() + 1 && !it.isDone }
            if (tomorrowHw != null) {
                "Завтра: ${tomorrowHw.subject.ifBlank { "Д/З" }} — ${tomorrowHw.description}"
            } else {
                "Все уроки на сегодня окончены"
            }
        }
        else -> {
            val dayHw = homework.firstOrNull { it.epochDay == date.toEpochDay() && !it.isDone }
            if (dayHw != null) {
                "${dayHw.subject.ifBlank { "Д/З" }}: ${dayHw.description}"
            } else if (ordered.isNotEmpty()) {
                val start = bells.of(ordered.first().period).start
                if (start.isNotBlank()) "Начало занятий в $start" else "Всего уроков: $count"
            } else {
                "Свободный день"
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Hero Banner Card
        item {
            HeroBannerCard(
                title = heroTitle,
                subtitle = heroSubtitle,
            )
            Spacer(Modifier.height(4.dp))
        }

        if (ordered.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "В расписании пусто",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Нажмите кнопку ниже, чтобы добавить уроки",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(18.dp))
                    FilledTonalButton(
                        onClick = onAddLesson,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить урок", fontSize = 15.sp)
                    }
                }
            }
        } else {
            items(ordered, key = { it.period }) { lesson ->
                val slot = bells.of(lesson.period)
                val time = slot.start.ifBlank { "№${lesson.period}" }
                val hw = homework.firstOrNull {
                    it.epochDay == date.toEpochDay() && it.period == lesson.period
                }
                val homeworkOrRoom = when {
                    hw != null && hw.description.isNotBlank() -> hw.description
                    lesson.room.isNotBlank() -> "Кабинет ${lesson.room}"
                    else -> ""
                }
                val gradeForLesson = grades.firstOrNull {
                    it.subject.equals(lesson.subject, ignoreCase = true) && it.epochDay == date.toEpochDay()
                }
                val subjectIcon = remember(lesson.subject, subjectIcons) {
                    SubjectIcon.resolve(lesson.subject, subjectIcons)
                }

                Md3LessonCard(
                    time = time,
                    subject = lesson.subject,
                    homeworkOrRoom = homeworkOrRoom,
                    grade = gradeForLesson?.value,
                    icon = subjectIcon,
                    onIconClick = { onPickIcon(lesson.subject) },
                    onClick = { onLessonClick(lesson) },
                )
            }
        }
    }
}

@Composable
private fun LessonActionDialog(
    lesson: Lesson,
    date: LocalDate,
    existingGrade: Grade?,
    existingHomework: Homework?,
    subjectIcons: Map<String, String>,
    onDismiss: () -> Unit,
    onPickIcon: () -> Unit,
    onOpenHomework: () -> Unit,
    onPickGrade: () -> Unit,
    onEditLesson: () -> Unit,
    onDeleteLesson: () -> Unit,
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
                text = lesson.subject,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            val roomSubtitle = if (lesson.room.isNotBlank()) "Кабинет ${lesson.room} · Урок ${lesson.period}" else "Урок ${lesson.period}"
            Text(
                text = roomSubtitle,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            // 1. Homework button
            FilledTonalButton(
                onClick = onOpenHomework,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Assignment, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (existingHomework != null) "Изменить задание" else "Добавить задание",
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(10.dp))

            // 2. Grade button
            FilledTonalButton(
                onClick = onPickGrade,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Grade, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (existingGrade != null) "Оценка: ${existingGrade.value} (изменить)" else "Поставить оценку",
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(10.dp))

            // 3. Subject icon button
            val currentIcon = remember(lesson.subject, subjectIcons) {
                SubjectIcon.resolve(lesson.subject, subjectIcons)
            }
            FilledTonalButton(
                onClick = onPickIcon,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    painter = painterResource(id = currentIcon.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Значок предмета",
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(10.dp))

            // 4. Edit lesson button
            OutlinedButton(
                onClick = onEditLesson,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Редактировать урок", fontSize = 16.sp)
            }

            Spacer(Modifier.height(10.dp))

            // 4. Delete lesson button
            TextButton(
                onClick = onDeleteLesson,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Удалить урок", fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun QuickGradePickerDialog(
    subject: String,
    existingGrade: Grade?,
    onDismiss: () -> Unit,
    onSelectGrade: (Int) -> Unit,
    onDeleteGrade: () -> Unit,
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
                text = "Оценка",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subject,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                listOf(5, 4, 3, 2).forEach { gradeNum ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(BlobShape)
                            .background(
                                if (existingGrade?.value == gradeNum) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                },
                            )
                            .clickable { onSelectGrade(gradeNum) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = gradeNum.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (existingGrade?.value == gradeNum) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            },
                        )
                    }
                }
            }

            if (existingGrade != null) {
                Spacer(Modifier.height(20.dp))
                TextButton(
                    onClick = onDeleteGrade,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Удалить оценку", fontSize = 15.sp)
                }
            }
        }
    }
}

private fun lessonWord(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "уроков"
        mod10 == 1 -> "урок"
        mod10 in 2..4 -> "урока"
        else -> "уроков"
    }
}
