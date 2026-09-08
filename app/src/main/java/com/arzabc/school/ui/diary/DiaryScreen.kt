package com.arzabc.school.ui.diary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.NowStatus
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.nowStatus
import com.arzabc.school.ui.components.CenteredChoiceDialog
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.PeriodBadge
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.WeekDayChips
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
    weekBells: WeekBells,
    schoolDays: Int,
    onSelectDate: (LocalDate) -> Unit,
    onEditLesson: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onAddHomework: (lesson: Lesson) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
    onOpenMenu: () -> Unit,
) {
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
            title = Dates.weekdayName(selectedDate),
            subtitle = stringResource(R.string.diary_week_grid),
            onOpenMenu = onOpenMenu,
        )
        WeekDayChips(
            selectedDate = selectedDate,
            weekDates = weekDates,
            today = today,
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
                bells = weekBells.forDay(Dates.schoolDayOfWeek(date)),
                currentPeriod = if (date == today) currentPeriod else null,
                onEditLesson = onEditLesson,
                onAddHomework = onAddHomework,
                onAddLesson = onAddLesson,
            )
        }
    }
}

@Composable
private fun DiaryPage(
    date: LocalDate,
    lessons: List<Lesson>,
    bells: BellSchedule,
    currentPeriod: Int?,
    onEditLesson: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onAddHomework: (lesson: Lesson) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
) {
    val ordered = remember(lessons) { lessons.sortedBy { it.period } }
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
                val slot = bells.of(lesson.period)
                LessonCard(
                    lesson = lesson,
                    startTime = slot.start,
                    endTime = slot.end,
                    happening = lesson.period == currentPeriod,
                    onEditLesson = { onEditLesson(date, lesson.period, lesson) },
                    onAddHomework = { onAddHomework(lesson) },
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    startTime: String,
    endTime: String,
    happening: Boolean,
    onEditLesson: () -> Unit,
    onAddHomework: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    var menu by remember { mutableStateOf(false) }
    val room = if (lesson.room.isNotBlank()) {
        stringResource(R.string.room_short, lesson.room)
    } else {
        ""
    }
    val time = when {
        startTime.isNotBlank() && endTime.isNotBlank() -> "$startTime–$endTime"
        else -> startTime
    }
    DiaryCard(highlighted = happening, onLongClick = { menu = true }) {
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
    }
    if (menu) {
        CenteredChoiceDialog(
            title = lesson.subject,
            onDismiss = { menu = false },
        ) {
            FilledTonalButton(
                onClick = {
                    menu = false
                    onAddHomework()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.add_homework))
            }
            Spacer(Modifier.size(8.dp))
            FilledTonalButton(
                onClick = {
                    menu = false
                    onEditLesson()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.edit_lesson))
            }
        }
    }
}
