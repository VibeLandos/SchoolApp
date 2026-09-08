package com.arzabc.school.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object SchoolCatalog {
    const val PERIODS = 8

    fun nextFreePeriod(used: Set<Int>): Int {
        for (period in 1..PERIODS) {
            if (period !in used) return period
        }
        if (0 !in used) return 0
        var period = PERIODS + 1
        while (period in used) period++
        return period
    }

    fun bells(period: Int): Pair<String, String> = when (period) {
        1 -> "08:00" to "08:45"
        2 -> "08:55" to "09:40"
        3 -> "09:50" to "10:35"
        4 -> "10:55" to "11:40"
        5 -> "11:50" to "12:35"
        6 -> "12:45" to "13:30"
        7 -> "13:40" to "14:25"
        8 -> "14:35" to "15:20"
        else -> "08:00" to "08:45"
    }
}

object Dates {
    private fun locale(): Locale = Locale.getDefault()

    fun todaySchoolDate(schoolDays: Int = 6): LocalDate =
        clampToSchoolWeek(LocalDate.now(), schoolDays)

    fun isWeekend(date: LocalDate, schoolDays: Int): Boolean {
        val day = date.dayOfWeek.value
        return if (schoolDays == 5) day >= 6 else day == 7
    }

    fun clampToSchoolWeek(date: LocalDate, schoolDays: Int): LocalDate {
        if (!isWeekend(date, schoolDays)) return date
        return if (date.dayOfWeek == DayOfWeek.SATURDAY) date.plusDays(2) else date.plusDays(1)
    }

    /** Next or previous school day, skipping the weekend for the current week length. */
    fun shiftSchoolDay(date: LocalDate, days: Int, schoolDays: Int): LocalDate {
        if (days == 0) return clampToSchoolWeek(date, schoolDays)
        val step = if (days > 0) 1L else -1L
        var cursor = date
        repeat(kotlin.math.abs(days)) {
            do {
                cursor = cursor.plusDays(step)
            } while (isWeekend(cursor, schoolDays))
        }
        return cursor
    }

    fun schoolDayOfWeek(date: LocalDate): Int = date.dayOfWeek.value

    fun weekDates(date: LocalDate, schoolDays: Int = 6): List<LocalDate> {
        val monday = weekMonday(date)
        val count = schoolDays.coerceIn(5, 6)
        return (0 until count).map { monday.plusDays(it.toLong()) }
    }

    fun formatFull(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale()))

    fun formatDayMonth(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("d MMM", locale()))

    fun weekdayName(date: LocalDate): String {
        val loc = locale()
        return date.dayOfWeek.getDisplayName(TextStyle.FULL, loc)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(loc) else it.toString() }
    }

    fun weekdayName(dayOfWeek: Int): String =
        weekdayName(LocalDate.now().with(DayOfWeek.of(dayOfWeek.coerceIn(1, 7))))

    /** Next dates for a repeating weekday, from today through the school year (or a few weeks if the year already ended). */
    fun upcomingWeekdays(
        dayOfWeek: Int,
        from: LocalDate = LocalDate.now(),
        until: LocalDate,
        limit: Int = 10,
    ): List<LocalDate> {
        val dow = DayOfWeek.of(dayOfWeek.coerceIn(1, 7))
        var cursor = from
        while (cursor.dayOfWeek != dow) cursor = cursor.plusDays(1)
        val last = if (until.isBefore(cursor)) cursor.plusWeeks((limit - 1).toLong()) else until
        val dates = ArrayList<LocalDate>(limit)
        while (!cursor.isAfter(last) && dates.size < limit) {
            dates.add(cursor)
            cursor = cursor.plusWeeks(1)
        }
        return dates
    }

    /** This weekday on the current week (if still ahead) and the same weekday next week. */
    fun datesForHomeworkPick(dayOfWeek: Int, from: LocalDate = LocalDate.now()): List<LocalDate> {
        val thisWeek = weekMonday(from).plusDays((dayOfWeek.coerceIn(1, 7) - 1).toLong())
        val nextWeek = thisWeek.plusWeeks(1)
        return listOfNotNull(
            thisWeek.takeIf { !it.isBefore(from) },
            nextWeek,
        ).distinct()
    }

    fun weekdayShort(date: LocalDate): String {
        val loc = locale()
        return date.dayOfWeek.getDisplayName(TextStyle.SHORT, loc)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(loc) else it.toString() }
    }

    fun weekRangeLabel(week: List<LocalDate>): String {
        val loc = locale()
        val month = DateTimeFormatter.ofPattern("MMMM", loc)
        val start = week.first()
        val end = week.last()
        return if (start.month == end.month) {
            "${start.dayOfMonth}–${end.dayOfMonth} ${start.format(month)}"
        } else {
            "${start.dayOfMonth} ${start.format(month)} – ${end.dayOfMonth} ${end.format(month)}"
        }
    }

    fun weekMonday(date: LocalDate): LocalDate {
        val school = if (date.dayOfWeek == DayOfWeek.SUNDAY) date.plusDays(1) else date
        return school.minusDays((school.dayOfWeek.value - 1).toLong())
    }

    fun dayIndex(date: LocalDate, schoolDays: Int = 6): Int =
        (schoolDayOfWeek(date) - 1).coerceIn(0, schoolDays.coerceIn(5, 6) - 1)
}

fun subjectFor(homework: Homework, lessons: List<Lesson>): String? {
    if (homework.subject.isNotBlank()) return homework.subject
    val dayOfWeek = LocalDate.ofEpochDay(homework.epochDay).dayOfWeek.value
    return lessons.find { it.dayOfWeek == dayOfWeek && it.period == homework.period }?.subject
}
