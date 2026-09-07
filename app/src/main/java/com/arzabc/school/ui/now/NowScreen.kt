package com.arzabc.school.ui.now

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
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay

@Composable
fun NowScreen(
    lessons: List<Lesson>,
    weekBells: WeekBells,
    schoolDays: Int,
    onOpenMenu: () -> Unit,
) {
    var today by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now()) }
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
            Column(Modifier.padding(horizontal = 16.dp)) {
                when (status) {
                    NowStatus.Sunday -> NowIdleCard(stringResource(R.string.now_sunday))
                    NowStatus.NoLessons -> NowIdleCard(stringResource(R.string.now_no_lessons))
                    is NowStatus.Before -> NowHero(
                        eyebrow = stringResource(R.string.now_until_start),
                        title = stringResource(
                            R.string.now_in_lesson,
                            status.first.period,
                            status.first.subject,
                        ),
                        timerLabel = stringResource(R.string.now_until_start),
                        minutes = status.minutes,
                        untilDayEnd = status.untilDayEnd,
                        progress = 0f,
                        meta = lessonMeta(status.first),
                    )
                    is NowStatus.InLesson -> NowHero(
                        eyebrow = stringResource(R.string.now_current),
                        title = stringResource(
                            R.string.now_in_lesson,
                            status.current.period,
                            status.current.subject,
                        ),
                        timerLabel = stringResource(R.string.now_until_bell),
                        minutes = status.minutes,
                        untilDayEnd = status.untilDayEnd,
                        progress = spanProgress(status.current.start, status.current.end, time),
                        meta = lessonMeta(status.current),
                    )
                    is NowStatus.InBreak -> NowHero(
                        eyebrow = stringResource(R.string.now_break),
                        title = stringResource(R.string.now_break),
                        timerLabel = stringResource(R.string.now_until_break_end),
                        minutes = status.minutes,
                        untilDayEnd = status.untilDayEnd,
                        progress = spanProgress(status.breakStart, status.next.start, time),
                        meta = stringResource(
                            R.string.now_in_lesson,
                            status.next.period,
                            status.next.subject,
                        ) + " · " + formatHm(status.next.start) + "–" + formatHm(status.next.end),
                    )
                    is NowStatus.After -> NowIdleCard(
                        title = stringResource(R.string.now_done),
                        subtitle = stringResource(R.string.now_last_bell, status.lastEnd),
                    )
                }
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
                    modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
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
    }
}

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
private fun NowIdleCard(title: String, subtitle: String? = null) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val muted = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    DiaryCard {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = muted,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun NowHero(
    eyebrow: String,
    title: String,
    timerLabel: String,
    minutes: Int,
    untilDayEnd: Int,
    progress: Float,
    meta: String? = null,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val muted = if (glass) tokens.textSecondary else scheme.onPrimaryContainer.copy(alpha = 0.85f)
    DiaryCard(highlighted = !glass) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (glass) tokens.accent else scheme.onPrimaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 6.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                meta?.let {
                    Text(
                        text = it,
                        color = muted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = timerLabel,
                    fontSize = 13.sp,
                    color = muted,
                    modifier = Modifier.padding(top = 12.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (untilDayEnd > 0) {
                    Text(
                        text = stringResource(R.string.now_until_day_end, remainingLabel(untilDayEnd)),
                        fontSize = 13.sp,
                        color = muted,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            LessonProgressRing(progress = progress, diameter = 96.dp, stroke = 7.dp) {
                RingCountdown(minutes)
            }
        }
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
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.now_ring_minutes, rest),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        } else {
            Text(
                text = rest.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.now_ring_min_unit),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
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
        modifier = modifier.alpha(if (done) 0.55f else 1f),
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

@Composable
private fun remainingLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return if (hours > 0) {
        stringResource(R.string.now_hours_minutes, hours, rest)
    } else {
        stringResource(R.string.now_minutes, minutes)
    }
}
