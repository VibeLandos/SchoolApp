package com.arzabc.school.ui.now

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
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
import com.arzabc.school.ui.theme.LocalDiaryPalette
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun NowScreen(
    lessons: List<Lesson>,
    weekBells: WeekBells,
    schoolDays: Int,
    onOpenMenu: () -> Unit,
) {
    val palette = LocalDiaryPalette.current
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
    val locale = Locale.getDefault()

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
                text = stringResource(R.string.nav_now),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 20.dp, top = 12.dp, bottom = 12.dp),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .background(palette.paper, RoundedCornerShape(2.dp))
                .border(1.dp, palette.paperLine, RoundedCornerShape(2.dp))
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        ) {
            Text(
                text = Dates.weekdayName(today).uppercase(locale),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = palette.ink,
            )
            Text(
                text = Dates.formatFull(today),
                fontFamily = FontFamily.Serif,
                fontSize = 14.sp,
                color = palette.faintInk,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            when (status) {
                NowStatus.Sunday -> {
                    Text(
                        stringResource(R.string.now_sunday),
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        color = palette.ink,
                    )
                }
                NowStatus.NoLessons -> {
                    Text(
                        stringResource(R.string.now_no_lessons),
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = palette.ink,
                    )
                }
                is NowStatus.Before -> {
                    Text(
                        stringResource(R.string.now_until_start),
                        color = palette.faintInk,
                        fontSize = 14.sp,
                    )
                    HeroMinutes(status.minutes)
                    Text(
                        text = stringResource(
                            R.string.now_next_lesson,
                            status.first.period,
                            status.first.subject,
                            formatHm(status.first.start),
                            formatHm(status.first.end),
                        ),
                        color = palette.ink,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    UntilDayEnd(status.untilDayEnd)
                    RemainingList(status.remaining, currentPeriod = null)
                }
                is NowStatus.InLesson -> {
                    Text(
                        text = stringResource(
                            R.string.now_in_lesson,
                            status.current.period,
                            status.current.subject,
                        ),
                        color = palette.ink,
                        fontSize = 16.sp,
                    )
                    val room = status.current.room
                    Text(
                        text = buildString {
                            append(formatHm(status.current.start))
                            append("–")
                            append(formatHm(status.current.end))
                            if (room.isNotBlank()) {
                                append(" · ")
                                append(stringResource(R.string.room_short, room))
                            }
                        },
                        color = palette.faintInk,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    HeroMinutes(status.minutes)
                    Text(
                        stringResource(R.string.now_until_lesson_end),
                        color = palette.faintInk,
                        fontSize = 14.sp,
                    )
                    UntilDayEnd(status.untilDayEnd)
                    RemainingList(status.remaining, currentPeriod = status.current.period)
                }
                is NowStatus.InBreak -> {
                    Text(
                        stringResource(R.string.now_break),
                        color = palette.ink,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = stringResource(
                            R.string.now_break_between,
                            status.afterPeriod,
                            status.next.period,
                        ),
                        color = palette.faintInk,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    HeroMinutes(status.minutes)
                    Text(
                        stringResource(R.string.now_until_break_end),
                        color = palette.faintInk,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = stringResource(
                            R.string.now_next_lesson,
                            status.next.period,
                            status.next.subject,
                            formatHm(status.next.start),
                            formatHm(status.next.end),
                        ),
                        color = palette.ink,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    UntilDayEnd(status.untilDayEnd)
                    RemainingList(status.remaining, currentPeriod = null)
                }
                is NowStatus.After -> {
                    Text(
                        stringResource(R.string.now_done),
                        fontFamily = FontFamily.Serif,
                        fontSize = 22.sp,
                        color = palette.ink,
                    )
                    Text(
                        text = stringResource(R.string.now_last_bell, status.lastEnd),
                        color = palette.faintInk,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMinutes(minutes: Int) {
    val palette = LocalDiaryPalette.current
    Text(
        text = remainingLabel(minutes),
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        color = palette.ink,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun UntilDayEnd(minutes: Int) {
    if (minutes <= 0) return
    val palette = LocalDiaryPalette.current
    Text(
        text = stringResource(R.string.now_until_day_end, remainingLabel(minutes)),
        color = palette.faintInk,
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun RemainingList(
    remaining: List<TimedLesson>,
    currentPeriod: Int?,
) {
    if (remaining.isEmpty()) return
    val palette = LocalDiaryPalette.current
    Spacer(
        Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(palette.paperLine),
    )
    remaining.forEach { item ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.period.toString(),
                color = palette.marginRed,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.width(36.dp),
            )
            Text(
                text = item.subject,
                color = palette.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (item.period == currentPeriod) {
                    stringResource(R.string.now_current)
                } else {
                    formatHm(item.start)
                },
                color = if (item.period == currentPeriod) palette.marginRed else palette.faintInk,
                fontSize = 14.sp,
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
