package com.example.firstandroidap.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object SchoolCatalog {
    val subjects = listOf(
        "Математика",
        "Алгебра",
        "Геометрия",
        "Русский язык",
        "Литература",
        "Английский язык",
        "История",
        "Обществознание",
        "География",
        "Биология",
        "Физика",
        "Химия",
        "Информатика",
        "Физкультура",
        "ОБЖ",
        "Музыка",
        "ИЗО",
        "Технология",
    )

    const val PERIODS = 8

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
    private val ru = Locale("ru")
    private val fullDate = DateTimeFormatter.ofPattern("d MMMM yyyy", ru)
    private val month = DateTimeFormatter.ofPattern("MMMM", ru)

    fun todaySchoolDate(): LocalDate {
        val today = LocalDate.now()
        return if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1) else today
    }

    fun schoolDayOfWeek(date: LocalDate): Int = date.dayOfWeek.value

    fun weekDates(date: LocalDate): List<LocalDate> {
        val monday = date.minusDays((date.dayOfWeek.value - 1).toLong())
        return (0..5).map { monday.plusDays(it.toLong()) }
    }

    fun formatFull(date: LocalDate): String = date.format(fullDate)

    fun weekdayName(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.FULL, ru)
            .replaceFirstChar { it.titlecase(ru) }

    fun weekdayShort(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, ru)
            .replaceFirstChar { it.titlecase(ru) }

    fun weekRangeLabel(week: List<LocalDate>): String {
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

    fun dayIndex(date: LocalDate): Int = (schoolDayOfWeek(date) - 1).coerceIn(0, 5)
}

fun subjectFor(homework: Homework, lessons: List<Lesson>): String {
    val dayOfWeek = LocalDate.ofEpochDay(homework.epochDay).dayOfWeek.value
    return lessons.find { it.dayOfWeek == dayOfWeek && it.period == homework.period }
        ?.subject
        ?: "Урок ${homework.period}"
}
