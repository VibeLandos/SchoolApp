package com.example.firstandroidap.ui.diary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.HomeworkPhoto
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.SchoolCatalog
import com.example.firstandroidap.ui.theme.CoverGold
import com.example.firstandroidap.ui.theme.FaintInk
import com.example.firstandroidap.ui.theme.HomeworkInk
import com.example.firstandroidap.ui.theme.Ink
import com.example.firstandroidap.ui.theme.MarginRed
import com.example.firstandroidap.ui.theme.Paper
import com.example.firstandroidap.ui.theme.PaperLine
import java.time.LocalDate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val PagerCount = 6

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryScreen(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    lessons: List<Lesson>,
    homework: List<Homework>,
    photos: List<HomeworkPhoto>,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
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
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            beyondBoundsPageCount = 1,
        ) { page ->
            val date = weekDates.getOrNull(page) ?: selectedDate
            DiaryPage(
                date = date,
                lessons = lessonsByDay[Dates.schoolDayOfWeek(date)].orEmpty(),
                homework = homeworkByDay[date.toEpochDay()].orEmpty(),
                photosByHomework = photosByHomework,
                onSubjectClick = onSubjectClick,
                onHomeworkClick = onHomeworkClick,
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
) {
    Column(Modifier.padding(bottom = 4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onShiftWeek(-1) }) {
                Icon(
                    Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Предыдущая неделя",
                    tint = CoverGold,
                )
            }
            Text(
                text = Dates.weekRangeLabel(weekDates),
                color = CoverGold,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onShiftWeek(1) }) {
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Следующая неделя",
                    tint = CoverGold,
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
                        color = CoverGold.copy(alpha = if (selected) 1f else 0.7f),
                        fontSize = 12.sp,
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (selected) CoverDeepText else CoverGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = if (selected) {
                            Modifier
                                .background(CoverGold, RoundedCornerShape(10.dp))
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

private val CoverDeepText = Color(0xFF4C1C23)

@Composable
private fun DiaryPage(
    date: LocalDate,
    lessons: List<Lesson>,
    homework: List<Homework>,
    photosByHomework: Map<Long, List<HomeworkPhoto>>,
    onSubjectClick: (date: LocalDate, period: Int, lesson: Lesson?) -> Unit,
    onHomeworkClick: (date: LocalDate, period: Int, lesson: Lesson?, homework: Homework?) -> Unit,
) {
    val lessonByPeriod = remember(lessons) { lessons.associateBy { it.period } }
    val homeworkByPeriod = remember(homework) { homework.associateBy { it.period } }
    val weekday = remember(date) { Dates.weekdayName(date).uppercase() }
    val fullDate = remember(date) { Dates.formatFull(date) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Paper, RoundedCornerShape(2.dp))
            .border(1.dp, PaperLine, RoundedCornerShape(2.dp))
            .padding(top = 16.dp, bottom = 20.dp),
    ) {
        Text(
            text = weekday,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = Ink,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp),
        )
        Text(
            text = fullDate,
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            color = FaintInk,
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, bottom = 12.dp),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
        ) {
            Text(
                text = "№",
                color = MarginRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
            Text(
                text = "ПРЕДМЕТ",
                color = FaintInk,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.weight(0.42f),
            )
            Text(
                text = "ДОМАШНЕЕ ЗАДАНИЕ",
                color = FaintInk,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.weight(0.58f),
            )
        }
        Spacer(Modifier.height(4.dp))
        repeat(SchoolCatalog.PERIODS) { index ->
            val period = index + 1
            val lesson = lessonByPeriod[period]
            val item = homeworkByPeriod[period]
            DiaryRow(
                period = period,
                lesson = lesson,
                homework = item,
                photoCount = item?.let { photosByHomework[it.id]?.size } ?: 0,
                onSubjectClick = { onSubjectClick(date, period, lesson) },
                onHomeworkClick = { onHomeworkClick(date, period, lesson, item) },
            )
        }
        Text(
            text = "Нажми клетку, чтобы вписать. Листай день свайпом, неделю — стрелками.",
            color = FaintInk,
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
    photoCount: Int,
    onSubjectClick: () -> Unit,
    onHomeworkClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(end = 8.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = period.toString(),
            color = MarginRed,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(40.dp),
        )
        Column(
            Modifier
                .weight(0.42f)
                .clickable(onClick = onSubjectClick)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            if (lesson == null) {
                Text("предмет", color = FaintInk.copy(alpha = 0.7f), fontSize = 14.sp)
            } else {
                Text(
                    text = lesson.subject,
                    color = Ink,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    lesson.startTime.takeIf { it.isNotBlank() }?.let { start ->
                        if (lesson.endTime.isNotBlank()) "$start–${lesson.endTime}" else start
                    },
                    lesson.room.takeIf { it.isNotBlank() }?.let { "каб. $it" },
                ).joinToString(" · ")
                if (meta.isNotBlank()) {
                    Text(meta, color = FaintInk, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
        Spacer(
            Modifier
                .width(1.dp)
                .height(36.dp)
                .background(PaperLine),
        )
        Column(
            Modifier
                .weight(0.58f)
                .clickable(onClick = onHomeworkClick)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            when {
                homework == null -> {
                    Text("д/з", color = FaintInk.copy(alpha = 0.7f), fontSize = 14.sp, fontFamily = FontFamily.Serif)
                }
                else -> {
                    if (homework.description.isNotBlank()) {
                        Text(
                            text = homework.description,
                            color = if (homework.isDone) FaintInk else HomeworkInk,
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
                            text = "$photoCount фото",
                            color = FaintInk,
                            fontSize = 12.sp,
                        )
                    } else if (homework.description.isBlank()) {
                        Text("д/з", color = FaintInk.copy(alpha = 0.7f), fontSize = 14.sp, fontFamily = FontFamily.Serif)
                    }
                }
            }
        }
    }
}
