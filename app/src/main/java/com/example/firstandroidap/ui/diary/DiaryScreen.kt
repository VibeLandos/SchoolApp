package com.example.firstandroidap.ui.diary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstandroidap.data.BellSchedule
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.HomeworkPhoto
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.WeekBells
import com.example.firstandroidap.R
import com.example.firstandroidap.ui.theme.LocalDiaryPalette
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val PagerCount = 6
private val NumberColumnWidth = 40.dp
private val SubjectColumnWidth = 130.dp
private val SubjectTapZoneWidth = NumberColumnWidth + SubjectColumnWidth

private fun formatLessonMeta(startTime: String, endTime: String, roomPart: String): String {
    val hasStart = startTime.isNotBlank()
    val hasEnd = endTime.isNotBlank()
    val hasRoom = roomPart.isNotBlank()
    return when {
        hasStart && hasEnd && hasRoom -> "$startTime–$endTime · $roomPart"
        hasStart && hasEnd -> "$startTime–$endTime"
        hasStart && hasRoom -> "$startTime · $roomPart"
        hasStart -> startTime
        hasRoom -> roomPart
        else -> ""
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    lessons: List<Lesson>,
    homework: List<Homework>,
    photos: List<HomeworkPhoto>,
    weekBells: WeekBells,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
    onOpenMenu: () -> Unit,
) {
    val photosByHomework = remember(photos) { photos.groupBy { it.homeworkId } }
    val homeworkByDay = remember(homework) { homework.groupBy { it.epochDay } }
    val lessonsByDay = remember(lessons) { lessons.groupBy { it.dayOfWeek } }

    val startPage = Dates.dayIndex(selectedDate)
    val pagerState = rememberPagerState(initialPage = startPage, pageCount = { PagerCount })
    val pagerScope = rememberCoroutineScope()
    val weekDatesState = rememberUpdatedState(weekDates)
    val onSelectDateState = rememberUpdatedState(onSelectDate)

    LaunchedEffect(selectedDate) {
        val target = Dates.dayIndex(selectedDate)
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
        WeekStrip(
            selectedDate = selectedDate,
            weekDates = weekDates,
            onSelectDate = { date ->
                onSelectDate(date)
                val target = Dates.dayIndex(date)
                pagerScope.launch { pagerState.animateScrollToPage(target) }
            },
            onShiftWeek = onShiftWeek,
            onOpenMenu = onOpenMenu,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            beyondBoundsPageCount = 0,
        ) { page ->
            val date = weekDates.getOrNull(page) ?: selectedDate
            DiaryPage(
                date = date,
                lessons = lessonsByDay[Dates.schoolDayOfWeek(date)].orEmpty(),
                homework = homeworkByDay[date.toEpochDay()].orEmpty(),
                photosByHomework = photosByHomework,
                bells = weekBells.forDay(Dates.schoolDayOfWeek(date)),
                onSubjectClick = onSubjectClick,
                onHomeworkClick = onHomeworkClick,
                onAddLesson = onAddLesson,
            )
        }
    }
}

@Composable
private fun WeekStrip(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onOpenMenu: () -> Unit,
) {
    val palette = LocalDiaryPalette.current
    Column(Modifier.padding(bottom = 4.dp)) {
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
            IconButton(onClick = { onShiftWeek(-1) }) {
                Icon(
                    Icons.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.cd_prev_week),
                    tint = palette.gold,
                )
            }
            Text(
                text = Dates.weekRangeLabel(weekDates),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onShiftWeek(1) }) {
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.cd_next_week),
                    tint = palette.gold,
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            weekDates.forEach { date ->
                val selected = date == selectedDate
                Column(
                    modifier = Modifier
                        .clickable { onSelectDate(date) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = Dates.weekdayShort(date),
                        color = palette.gold.copy(alpha = if (selected) 1f else 0.7f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (selected) palette.onGold else palette.gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = if (selected) {
                            Modifier
                                .background(palette.gold, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        } else {
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        },
                    )
                }
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
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
    onAddLesson: (date: LocalDate) -> Unit,
) {
    val ordered = remember(lessons) { lessons.sortedBy { it.period } }
    val homeworkByPeriod = remember(homework) { homework.associateBy { it.period } }
    val locale = Locale.getDefault()
    val weekday = remember(date, locale) { Dates.weekdayName(date).uppercase(locale) }
    val fullDate = remember(date, locale) { Dates.formatFull(date) }
    val palette = LocalDiaryPalette.current

    Column(
        Modifier
            .fillMaxSize()
            .background(palette.paper, RoundedCornerShape(2.dp))
            .border(1.dp, palette.paperLine, RoundedCornerShape(2.dp))
            .padding(top = 16.dp, bottom = 20.dp),
    ) {
        Text(
            text = weekday,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = palette.ink,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp),
        )
        Text(
            text = fullDate,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            color = palette.faintInk,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, bottom = 12.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
        ) {
            Text(
                text = "№",
                color = palette.marginRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(NumberColumnWidth),
            )
            Text(
                text = stringResource(R.string.col_subject),
                color = palette.faintInk,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.width(SubjectColumnWidth),
            )
            Text(
                text = stringResource(R.string.col_homework),
                color = palette.faintInk,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.height(4.dp))
        if (ordered.isEmpty()) {
            Text(
                text = stringResource(R.string.diary_empty_day),
                color = palette.faintInk,
                fontSize = 15.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            )
        } else {
            ordered.forEach { lesson ->
                val item = homeworkByPeriod[lesson.period]
                val slot = bells.of(lesson.period)
                DiaryRow(
                    period = lesson.period,
                    lesson = lesson,
                    homework = item,
                    startTime = slot.start,
                    endTime = slot.end,
                    photoCount = item?.let { photosByHomework[it.id]?.size } ?: 0,
                    onSubjectClick = { onSubjectClick(date, lesson.period, lesson) },
                    onHomeworkClick = { onHomeworkClick(date, lesson.period, lesson, item) },
                )
            }
        }
        Text(
            text = stringResource(R.string.add_lesson),
            color = palette.marginRed,
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            modifier = Modifier
                .padding(start = 20.dp, top = 12.dp, end = 16.dp)
                .clickable { onAddLesson(date) },
        )
        Text(
            text = stringResource(R.string.diary_hint),
            color = palette.faintInk,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 16.dp),
        )
    }
}

@Composable
private fun DiaryRow(
    period: Int,
    lesson: Lesson?,
    homework: Homework?,
    startTime: String,
    endTime: String,
    photoCount: Int,
    onSubjectClick: () -> Unit,
    onHomeworkClick: () -> Unit,
) {
    val palette = LocalDiaryPalette.current
    val subjectClick = rememberUpdatedState(onSubjectClick)
    val homeworkClick = rememberUpdatedState(onHomeworkClick)
    val roomPart = if (!lesson?.room.isNullOrBlank()) {
        stringResource(R.string.room_short, lesson?.room.orEmpty())
    } else {
        ""
    }
    val meta = remember(startTime, endTime, roomPart) {
        if (lesson == null) "" else formatLessonMeta(startTime, endTime, roomPart)
    }
    val photoLabel = if (photoCount > 0) {
        pluralStringResource(R.plurals.photo_count, photoCount, photoCount)
    } else {
        ""
    }

    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(end = 8.dp, bottom = 1.dp)
            .pointerInput(Unit) {
                val zonePx = SubjectTapZoneWidth.toPx()
                detectTapGestures { offset ->
                    if (offset.x < zonePx) subjectClick.value() else homeworkClick.value()
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = period.toString(),
            color = palette.marginRed,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(NumberColumnWidth),
        )
        Column(
            Modifier
                .width(SubjectColumnWidth)
                .semantics {
                    role = Role.Button
                    onClick { onSubjectClick(); true }
                }
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (lesson == null) {
                Text(
                    stringResource(R.string.placeholder_subject),
                    color = palette.faintInk.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            } else {
                Text(
                    text = lesson.subject,
                    color = palette.ink,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (meta.isNotEmpty()) {
                    Text(meta, color = palette.faintInk, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
        Spacer(
            Modifier
                .width(1.dp)
                .height(36.dp)
                .background(palette.paperLine),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                    onClick { onHomeworkClick(); true }
                }
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                homework == null -> {
                    Text(
                        stringResource(R.string.placeholder_homework),
                        color = palette.faintInk.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                    )
                }
                else -> {
                    if (homework.description.isNotBlank()) {
                        Text(
                            text = homework.description,
                            color = if (homework.isDone) palette.faintInk else palette.homeworkInk,
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            maxLines = 2,
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
                            text = photoLabel,
                            color = palette.faintInk,
                            fontSize = 12.sp,
                        )
                    } else if (homework.description.isBlank()) {
                        Text(
                            stringResource(R.string.placeholder_homework),
                            color = palette.faintInk.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                        )
                    }
                }
            }
        }
    }
}
