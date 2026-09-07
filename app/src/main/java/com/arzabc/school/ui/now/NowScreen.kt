package com.arzabc.school.ui.now

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.NowStatus
import com.arzabc.school.data.SchoolYear
import com.arzabc.school.data.TimedLesson
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.formatHm
import com.arzabc.school.data.lessonSpanProgress
import com.arzabc.school.data.nowStatus
import com.arzabc.school.data.spanProgress
import com.arzabc.school.data.timedLessonsForDay
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.LessonProgressRing
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.SchoolYearSheet
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import kotlinx.coroutines.delay

@Composable
fun NowScreen(
    lessons: List<Lesson>,
    weekBells: WeekBells,
    schoolDays: Int,
    schoolYear: SchoolYear,
    onSaveSchoolYear: (SchoolYear) -> Unit,
    onOpenMenu: () -> Unit,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
    var editingYear by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            today = LocalDate.now()
            time = LocalTime.now()
            delay(1_000)
        }
    }
    val bells = weekBells.forDay(today.dayOfWeek.value)
    val status = remember(today, time, lessons, bells, schoolDays) {
        nowStatus(today, time, lessons, bells, schoolDays)
    }
    val todayLessons = remember(today, lessons, bells, schoolDays) {
        timedLessonsForDay(today, lessons, bells, schoolDays)
    }
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val muted = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val currentPeriod = (status as? NowStatus.InLesson)?.current?.period

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            ScreenHeader(
                title = stringResource(R.string.nav_now),
                subtitle = stringResource(
                    R.string.date_with_weekday,
                    Dates.weekdayName(today),
                    Dates.formatDayMonth(today),
                ),
                onOpenMenu = onOpenMenu,
            )
        }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                NowStatusRing(status = status, time = time)
            }
        }
        if (todayLessons.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.now_today_lessons),
                    color = muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                )
            }
            items(todayLessons, key = { it.period }) { item ->
                LessonNowRow(
                    item = item,
                    progress = lessonSpanProgress(item, time),
                    happening = item.period == currentPeriod,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
        }
        item {
            YearCountdownCard(
                today = today,
                year = schoolYear,
                onClick = { editingYear = true },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            )
        }
    }

    if (editingYear) {
        SchoolYearSheet(
            year = schoolYear,
            onDismiss = { editingYear = false },
            onSave = onSaveSchoolYear,
        )
    }
}

@Composable
private fun NowStatusRing(status: NowStatus, time: LocalTime) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val muted = if (glass) tokens.textSecondary else scheme.onSurfaceVariant
    val ring = when (status) {
        NowStatus.Sunday -> RingContent(
            progress = 0f,
            title = stringResource(R.string.now_sunday),
            subtitle = null,
            minutes = null,
            caption = null,
        )
        NowStatus.NoLessons -> RingContent(
            progress = 0f,
            title = stringResource(R.string.now_no_lessons),
            subtitle = null,
            minutes = null,
            caption = null,
        )
        is NowStatus.Before -> RingContent(
            progress = 0f,
            title = stringResource(R.string.now_in_lesson, status.first.period, status.first.subject),
            subtitle = lessonMeta(status.first),
            minutes = status.minutes,
            caption = stringResource(R.string.now_until_start),
        )
        is NowStatus.InLesson -> RingContent(
            progress = spanProgress(status.current.start, status.current.end, time),
            title = stringResource(R.string.now_in_lesson, status.current.period, status.current.subject),
            subtitle = lessonMeta(status.current),
            minutes = status.minutes,
            caption = stringResource(R.string.now_until_bell),
        )
        is NowStatus.InBreak -> RingContent(
            progress = spanProgress(status.breakStart, status.next.start, time),
            title = stringResource(R.string.now_break),
            subtitle = stringResource(
                R.string.now_in_lesson,
                status.next.period,
                status.next.subject,
            ),
            minutes = status.minutes,
            caption = stringResource(R.string.now_until_break_end),
        )
        is NowStatus.After -> RingContent(
            progress = 1f,
            title = stringResource(R.string.now_done),
            subtitle = stringResource(R.string.now_last_bell, status.lastEnd),
            minutes = null,
            caption = null,
        )
    }
    LessonProgressRing(progress = ring.progress, diameter = 260.dp, stroke = 8.dp) {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (ring.minutes != null) {
                RingCountdown(ring.minutes)
                Text(
                    text = ring.caption.orEmpty(),
                    color = muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Text(
                text = ring.title,
                style = if (ring.minutes == null && ring.title.length < 28) {
                    MaterialTheme.typography.titleLarge
                } else {
                    MaterialTheme.typography.titleMedium
                },
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = if (ring.minutes == null) 0.dp else 8.dp),
            )
            ring.subtitle?.let {
                Text(
                    text = it,
                    color = muted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private data class RingContent(
    val progress: Float,
    val title: String,
    val subtitle: String?,
    val minutes: Int?,
    val caption: String?,
)

@Composable
private fun lessonMeta(lesson: TimedLesson): String {
    val range = "${formatHm(lesson.start)}–${formatHm(lesson.end)}"
    return if (lesson.room.isNotBlank()) {
        "$range · ${stringResource(R.string.room_short, lesson.room)}"
    } else {
        range
    }
}

@Composable
private fun RingCountdown(minutes: Int) {
    val hours = minutes / 60
    val rest = minutes % 60
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (hours > 0) {
            Text(
                text = stringResource(R.string.now_ring_hours, hours),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.now_ring_minutes, rest),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        } else {
            Text(
                text = rest.toString(),
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.now_ring_min_unit),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun YearCountdownCard(
    today: LocalDate,
    year: SchoolYear,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val muted = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val ended = !today.isBefore(year.end)
    val untilStart = today.isBefore(year.start)
    val target = when {
        ended -> year.end
        untilStart -> year.start
        else -> year.end
    }
    val period = Period.between(today, target).let { if (it.isNegative) Period.ZERO else it }
    val months = period.years * 12 + period.months
    val label = if (untilStart) {
        stringResource(R.string.year_until_start)
    } else {
        stringResource(R.string.year_until_end)
    }
    val remainingText = if (ended) {
        stringResource(R.string.year_ended)
    } else if (months > 0) {
        stringResource(R.string.year_months_days, months, period.days)
    } else {
        stringResource(R.string.year_days_only, period.days)
    }
    DiaryCard(modifier = modifier, onClick = onClick) {
        Text(
            text = label.uppercase(),
            color = if (glass) tokens.accent else MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )
        Text(
            text = remainingText,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = stringResource(
                R.string.year_range,
                Dates.formatDayMonth(year.start),
                Dates.formatFull(year.end),
            ),
            color = muted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(R.string.year_set_dates),
            color = muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun LessonNowRow(
    item: TimedLesson,
    progress: Float,
    happening: Boolean,
    modifier: Modifier = Modifier,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val muted = when {
        glass -> tokens.textSecondary
        happening -> scheme.onPrimaryContainer.copy(alpha = 0.75f)
        else -> scheme.onSurfaceVariant
    }
    val done = progress >= 1f && !happening
    DiaryCard(
        modifier = modifier.alpha(if (done) 0.7f else 1f),
        highlighted = happening,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LessonProgressRing(progress = progress, diameter = 44.dp, stroke = 3.5.dp) {
                Text(
                    text = item.period.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                )
            }
            Column(
                Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
            ) {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = lessonMeta(item),
                    color = muted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
