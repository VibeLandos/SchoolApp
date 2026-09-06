package com.arzabc.school.data

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class TimedLesson(
    val period: Int,
    val start: LocalTime,
    val end: LocalTime,
    val subject: String,
    val room: String,
)

sealed interface NowStatus {
    data object Sunday : NowStatus
    data object NoLessons : NowStatus
    data class Before(
        val minutes: Int,
        val first: TimedLesson,
        val untilDayEnd: Int,
        val remaining: List<TimedLesson>,
    ) : NowStatus
    data class InLesson(
        val current: TimedLesson,
        val minutes: Int,
        val untilDayEnd: Int,
        val remaining: List<TimedLesson>,
    ) : NowStatus
    data class InBreak(
        val afterPeriod: Int,
        val minutes: Int,
        val next: TimedLesson,
        val untilDayEnd: Int,
        val remaining: List<TimedLesson>,
    ) : NowStatus
    data class After(val lastEnd: String) : NowStatus
}

fun nowStatus(
    date: LocalDate,
    time: LocalTime,
    lessons: List<Lesson>,
    bells: BellSchedule,
    schoolDays: Int = 6,
): NowStatus {
    if (Dates.isWeekend(date, schoolDays)) return NowStatus.Sunday
    val day = Dates.schoolDayOfWeek(date)
    val filled = lessons
        .filter { it.dayOfWeek == day }
        .sortedBy { it.period }
        .mapNotNull { lesson ->
            val slot = bells.of(lesson.period)
            val start = parseHm(slot.start) ?: return@mapNotNull null
            val end = parseHm(slot.end) ?: return@mapNotNull null
            TimedLesson(lesson.period, start, end, lesson.subject, lesson.room)
        }
    if (filled.isEmpty()) return NowStatus.NoLessons
    val lastEnd = filled.last().end
    val untilDayEnd = minutesUntil(time, lastEnd)
    return when {
        time < filled.first().start -> NowStatus.Before(
            minutes = minutesUntil(time, filled.first().start),
            first = filled.first(),
            untilDayEnd = untilDayEnd,
            remaining = filled,
        )
        time >= lastEnd -> NowStatus.After(formatHm(lastEnd))
        else -> {
            filled.forEachIndexed { index, lesson ->
                if (time >= lesson.start && time < lesson.end) {
                    return NowStatus.InLesson(
                        current = lesson,
                        minutes = minutesUntil(time, lesson.end),
                        untilDayEnd = untilDayEnd,
                        remaining = filled.drop(index),
                    )
                }
                val next = filled.getOrNull(index + 1) ?: return@forEachIndexed
                if (time >= lesson.end && time < next.start) {
                    return NowStatus.InBreak(
                        afterPeriod = lesson.period,
                        minutes = minutesUntil(time, next.start),
                        next = next,
                        untilDayEnd = untilDayEnd,
                        remaining = filled.drop(index + 1),
                    )
                }
            }
            NowStatus.After(formatHm(lastEnd))
        }
    }
}

fun minutesUntil(from: LocalTime, to: LocalTime): Int {
    val seconds = Duration.between(from, to).seconds
    if (seconds <= 0) return 0
    return ((seconds + 59) / 60).toInt()
}
