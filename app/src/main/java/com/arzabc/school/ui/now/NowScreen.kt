package com.arzabc.school.ui.now

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.NowStatus
import com.arzabc.school.data.TimedLesson
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.formatHm
import com.arzabc.school.data.nowStatus
import com.arzabc.school.data.spanProgress
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.PeriodBadge
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.SpanProgressBar
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
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val muted = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
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
                    NowStatus.Sunday -> Text(stringResource(R.string.now_sunday), style = MaterialTheme.typography.titleLarge)
                    NowStatus.NoLessons -> Text(stringResource(R.string.now_no_lessons), style = MaterialTheme.typography.titleLarge)
                    is NowStatus.Before -> NowHero(
                        eyebrow = stringResource(R.string.now_until_start),
                        title = stringResource(
                            R.string.now_next_lesson,
                            status.first.period,
                            status.first.subject,
                            formatHm(status.first.start),
                            formatHm(status.first.end),
                        ),
                        timerLabel = stringResource(R.string.now_until_start),
                        minutes = status.minutes,
                        untilDayEnd = status.untilDayEnd,
                        progress = null,
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
                        meta = buildString {
                            append(formatHm(status.current.start))
                            append("–")
                            append(formatHm(status.current.end))
                            if (status.current.room.isNotBlank()) {
                                append(" · ")
                                append(stringResource(R.string.room_short, status.current.room))
                            }
                        },
                    )
                    is NowStatus.InBreak -> NowHero(
                        eyebrow = stringResource(R.string.now_break),
                        title = stringResource(R.string.now_break),
                        timerLabel = stringResource(R.string.now_until_break_end),
                        minutes = status.minutes,
                        untilDayEnd = status.untilDayEnd,
                        progress = spanProgress(status.breakStart, status.next.start, time),
                        meta = stringResource(
                            R.string.now_next_lesson,
                            status.next.period,
                            status.next.subject,
                            formatHm(status.next.start),
                            formatHm(status.next.end),
                        ),
                    )
                    is NowStatus.After -> {
                        Text(stringResource(R.string.now_done), style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = stringResource(R.string.now_last_bell, status.lastEnd),
                            color = muted,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
        val remaining = when (status) {
            is NowStatus.Before -> status.remaining
            is NowStatus.InLesson -> status.remaining
            is NowStatus.InBreak -> status.remaining
            else -> emptyList()
        }
        val currentPeriod = (status as? NowStatus.InLesson)?.current?.period
        if (remaining.isNotEmpty()) {
            item {
                Text(
                    text = if (status is NowStatus.InBreak) {
                        stringResource(R.string.now_next_up)
                    } else {
                        stringResource(R.string.now_remaining_today)
                    },
                    color = muted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                )
            }
            items(remaining, key = { it.period }) { item ->
                RemainingRow(
                    item = item,
                    happening = item.period == currentPeriod,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
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
    progress: Float?,
    meta: String? = null,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    DiaryCard(highlighted = !glass) {
        Text(
            text = eyebrow.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (glass) tokens.accent else scheme.onPrimaryContainer.copy(alpha = 0.8f),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        meta?.let {
            Text(
                text = it,
                color = if (glass) tokens.textSecondary else scheme.onPrimaryContainer.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Row(
            Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = timerLabel,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 10.dp, bottom = 4.dp),
            )
            Text(
                text = remainingLabel(minutes),
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (untilDayEnd > 0) {
            Text(
                text = stringResource(R.string.now_until_day_end, remainingLabel(untilDayEnd)),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        if (progress != null) {
            SpanProgressBar(progress)
        }
    }
}

@Composable
private fun RemainingRow(
    item: TimedLesson,
    happening: Boolean,
    modifier: Modifier = Modifier,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    DiaryCard(modifier = modifier, highlighted = happening) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PeriodBadge(item.period, highlighted = happening)
            Text(
                text = item.subject,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                maxLines = 1,
            )
            Text(
                text = if (happening) {
                    stringResource(R.string.now_current)
                } else {
                    formatHm(item.start)
                },
                color = if (glass) tokens.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
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
