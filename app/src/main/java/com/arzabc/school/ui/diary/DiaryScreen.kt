package com.arzabc.school.ui.diary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.NowStatus
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.nowStatus
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.PeriodBadge
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.time.LocalDate
import java.time.LocalTime
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
    photos: List<HomeworkPhoto>,
    weekBells: WeekBells,
    schoolDays: Int,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
    onToggleHomework: (Homework) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
    onOpenMenu: () -> Unit,
) {
    val photosByHomework = remember(photos) { photos.groupBy { it.homeworkId } }
    val homeworkByDay = remember(homework) { homework.groupBy { it.epochDay } }
    val lessonsByDay = remember(lessons) { lessons.groupBy { it.dayOfWeek } }
    var clock by remember { mutableStateOf(LocalTime.now() to LocalDate.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = LocalTime.now() to LocalDate.now()
            delay(15_000)
        }
    }
    val todayStatus = remember(clock, lessons, weekBells, schoolDays) {
        val (time, today) = clock
        nowStatus(today, time, lessons, weekBells.forDay(today.dayOfWeek.value), schoolDays)
    }
    val currentPeriod = (todayStatus as? NowStatus.InLesson)?.current?.period
    val today = clock.second

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

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(
                R.string.date_with_weekday,
                Dates.weekdayName(selectedDate),
                Dates.formatDayMonth(selectedDate),
            ),
            subtitle = Dates.weekRangeLabel(weekDates),
            onOpenMenu = onOpenMenu,
            actions = {
                val iconTint = if (isGlassStyle()) {
                    LocalGlassTokens.current.text
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
                IconButton(onClick = { onShiftWeek(-1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.cd_prev_week),
                        tint = iconTint,
                    )
                }
                IconButton(onClick = { onShiftWeek(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.cd_next_week),
                        tint = iconTint,
                    )
                }
            },
        )
        WeekChips(
            selectedDate = selectedDate,
            weekDates = weekDates,
            onSelectDate = { date ->
                onSelectDate(date)
                val target = Dates.dayIndex(date, schoolDays)
                pagerScope.launch { pagerState.animateScrollToPage(target) }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondBoundsPageCount = 0,
        ) { page ->
            val date = weekDates.getOrNull(page) ?: selectedDate
            DiaryPage(
                date = date,
                lessons = lessonsByDay[Dates.schoolDayOfWeek(date)].orEmpty(),
                homework = homeworkByDay[date.toEpochDay()].orEmpty(),
                photosByHomework = photosByHomework,
                bells = weekBells.forDay(Dates.schoolDayOfWeek(date)),
                currentPeriod = if (date == today) currentPeriod else null,
                onSubjectClick = onSubjectClick,
                onHomeworkClick = onHomeworkClick,
                onToggleHomework = onToggleHomework,
                onAddLesson = onAddLesson,
            )
        }
    }
}

@Composable
private fun WeekChips(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    onSelectDate: (LocalDate) -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        weekDates.forEach { date ->
            val selected = date == selectedDate
            val shape = RoundedCornerShape(if (glass) 14.dp else 12.dp)
            val bg = when {
                selected && glass && tokens.darkChrome -> Color.White.copy(alpha = 0.22f)
                selected && glass -> Color.White.copy(alpha = 0.92f)
                selected -> scheme.secondaryContainer
                glass -> tokens.subtle
                else -> scheme.surfaceContainerLow
            }
            val fg = when {
                selected && glass && tokens.darkChrome -> Color.White
                selected && glass -> Color(0xFF111111)
                selected -> scheme.onSecondaryContainer
                glass -> tokens.textSecondary
                else -> scheme.onSurfaceVariant
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(shape)
                    .background(bg)
                    .then(
                        if (glass || selected) {
                            Modifier.border(
                                1.dp,
                                if (selected && !glass) scheme.primary else if (glass) tokens.border else scheme.outlineVariant,
                                shape,
                            )
                        } else {
                            Modifier.border(1.dp, scheme.outlineVariant, shape)
                        },
                    )
                    .clickable { onSelectDate(date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = Dates.weekdayShort(date).uppercase(),
                    color = fg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    color = fg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DiaryPage(
    date: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    photosByHomework: Map<Long, List<HomeworkPhoto>>,
    bells: BellSchedule,
    currentPeriod: Int?,
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
    onToggleHomework: (Homework) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
) {
    val ordered = remember(lessons) { lessons.sortedBy { it.period } }
    val homeworkByPeriod = remember(homework) { homework.associateBy { it.period } }
    val bottomGap = if (isGlassStyle()) 28.dp else 88.dp
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomGap),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (ordered.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.diary_empty_day),
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isGlassStyle()) {
                            LocalGlassTokens.current.text
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                    Text(
                        text = stringResource(R.string.diary_empty_body),
                        color = if (isGlassStyle()) {
                            LocalGlassTokens.current.textSecondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
                    )
                    FilledTonalButton(
                        onClick = { onAddLesson(date) },
                        modifier = Modifier.padding(top = 20.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.add_lesson))
                    }
                }
            }
        } else {
            items(ordered, key = { it.period }) { lesson ->
                val item = homeworkByPeriod[lesson.period]
                val slot = bells.of(lesson.period)
                LessonCard(
                    lesson = lesson,
                    homework = item,
                    startTime = slot.start,
                    endTime = slot.end,
                    photoCount = item?.let { photosByHomework[it.id]?.size } ?: 0,
                    happening = lesson.period == currentPeriod,
                    onSubjectClick = { onSubjectClick(date, lesson.period, lesson) },
                    onHomeworkClick = { onHomeworkClick(date, lesson.period, lesson, item) },
                    onToggleHomework = { item?.let(onToggleHomework) },
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    homework: Homework?,
    startTime: String,
    endTime: String,
    photoCount: Int,
    happening: Boolean,
    onSubjectClick: () -> Unit,
    onHomeworkClick: () -> Unit,
    onToggleHomework: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val room = if (lesson.room.isNotBlank()) {
        stringResource(R.string.room_short, lesson.room)
    } else {
        ""
    }
    val time = when {
        startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime–$endTime"
        else -> startTime
    }
    DiaryCard(highlighted = happening, onClick = onSubjectClick) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PeriodBadge(lesson.period, highlighted = happening)
                Column(Modifier.padding(start = 10.dp).weight(1f)) {
                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (glass) tokens.text else LocalContentColor.current,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOf(time, room).filter { it.isNotBlank() }.joinToString(" · "),
                        color = if (glass) tokens.textSecondary else scheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                    )
                }
                if (happening) {
                    Text(
                        text = stringResource(R.string.now_happening),
                        color = if (glass) tokens.accent else scheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (homework != null) {
                val panelShape = RoundedCornerShape(12.dp)
                Row(
                    Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                        .clip(panelShape)
                        .background(
                            if (glass) tokens.subtle else scheme.surfaceContainerHigh,
                        )
                        .clickable(onClick = onHomeworkClick)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = homework.isDone,
                        onCheckedChange = { onToggleHomework() },
                    )
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(top = 10.dp, end = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.homework_tag),
                            color = if (glass) tokens.accent else scheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.4.sp,
                        )
                        if (homework.description.isNotBlank()) {
                            Text(
                                text = homework.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (glass) tokens.text else LocalContentColor.current,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (homework.isDone) {
                                    TextDecoration.LineThrough
                                } else {
                                    TextDecoration.None
                                },
                            )
                        }
                        if (photoCount > 0) {
                            Text(
                                text = pluralStringResource(R.plurals.photo_count, photoCount, photoCount),
                                color = if (glass) tokens.textSecondary else scheme.onSurfaceVariant,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
