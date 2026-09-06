package com.arzabc.school.data

data class DaySchedulePayload(
    val dayOfWeek: Int,
    val bells: BellSchedule,
    val lessons: List<Lesson>,
)

data class WeekSchedulePayload(
    val schoolDays: Int,
    val weekBells: WeekBells,
    val lessons: List<Lesson>,
)

sealed interface SchedulePayload {
    data class Day(val value: DaySchedulePayload) : SchedulePayload
    data class Week(val value: WeekSchedulePayload) : SchedulePayload
}

object ScheduleText {
    fun encodeDay(
        dayOfWeek: Int,
        lessons: List<Lesson>,
        bells: BellSchedule,
        weekdayTitle: String,
    ): String = buildString {
        appendLine(weekdayTitle)
        appendLine("#diary day")
        appendLine("dow:$dayOfWeek")
        appendSection("bells", bells)
        appendLessons("lessons", lessons.filter { it.dayOfWeek == dayOfWeek })
    }.trimEnd()

    fun encodeWeek(
        schoolDays: Int,
        lessons: List<Lesson>,
        weekBells: WeekBells,
        dayTitle: (Int) -> String,
    ): String = buildString {
        appendLine("#diary week")
        appendLine("days:${schoolDays.coerceIn(5, 6)}")
        appendSection("bells", weekBells.week)
        weekBells.overrides.entries.sortedBy { it.key }.forEach { (day, schedule) ->
            appendSection("bells $day", schedule)
        }
        for (day in 1..schoolDays.coerceIn(5, 6)) {
            appendLine(dayTitle(day))
            appendLessons("lessons $day", lessons.filter { it.dayOfWeek == day })
        }
    }.trimEnd()

    fun decode(raw: String): SchedulePayload? {
        val lines = raw.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val kind = lines.firstOrNull { it.startsWith("#diary") } ?: return null
        return when {
            kind.startsWith("#diary day") -> decodeDay(lines)?.let { SchedulePayload.Day(it) }
            kind.startsWith("#diary week") -> decodeWeek(lines)?.let { SchedulePayload.Week(it) }
            else -> null
        }
    }

    private fun StringBuilder.appendSection(name: String, bells: BellSchedule) {
        appendLine("[$name]")
        bells.periods.forEachIndexed { index, slot ->
            appendLine("${index + 1} ${slot.start}-${slot.end}")
        }
    }

    private fun StringBuilder.appendLessons(name: String, lessons: List<Lesson>) {
        appendLine("[$name]")
        lessons.sortedBy { it.period }.forEach { lesson ->
            if (lesson.room.isBlank()) {
                appendLine("${lesson.period} ${lesson.subject}")
            } else {
                appendLine("${lesson.period} ${lesson.subject} | ${lesson.room}")
            }
        }
    }

    private fun decodeDay(lines: List<String>): DaySchedulePayload? {
        val dow = lines.intAfter("dow:")?.coerceIn(1, 6) ?: return null
        val sections = sections(lines)
        val bells = parseBells(sections["bells"] ?: return null) ?: return null
        val lessons = parseLessons(sections["lessons"].orEmpty(), dow)
        return DaySchedulePayload(dow, bells, lessons)
    }

    private fun decodeWeek(lines: List<String>): WeekSchedulePayload? {
        val days = lines.intAfter("days:")?.coerceIn(5, 6) ?: 6
        val sections = sections(lines)
        val week = parseBells(sections["bells"] ?: return null) ?: return null
        val overrides = (1..6).mapNotNull { day ->
            val body = sections["bells $day"] ?: return@mapNotNull null
            val schedule = parseBells(body) ?: return@mapNotNull null
            day to schedule
        }.toMap()
        val lessons = (1..days).flatMap { day ->
            parseLessons(sections["lessons $day"].orEmpty(), day)
        }
        return WeekSchedulePayload(
            schoolDays = days,
            weekBells = WeekBells(week = week, overrides = overrides, setupDone = true),
            lessons = lessons,
        )
    }

    private fun sections(lines: List<String>): Map<String, List<String>> {
        val result = linkedMapOf<String, MutableList<String>>()
        var current: String? = null
        for (line in lines) {
            if (line.startsWith("[") && line.endsWith("]")) {
                current = line.substring(1, line.length - 1).trim().lowercase()
                result[current] = mutableListOf()
            } else if (current != null && !line.startsWith("#") && !line.startsWith("dow:") && !line.startsWith("days:")) {
                result.getValue(current).add(line)
            }
        }
        return result
    }

    private fun parseBells(lines: List<String>): BellSchedule? {
        val slots = lines.mapNotNull { line ->
            val match = BELL.matchEntire(line) ?: return@mapNotNull null
            val start = match.groupValues[2]
            val end = match.groupValues[3]
            if (parseHm(start) == null || parseHm(end) == null) return@mapNotNull null
            match.groupValues[1].toInt() to BellPeriod(start, end)
        }.sortedBy { it.first }
        if (slots.isEmpty()) return null
        return BellSchedule(slots.map { it.second })
    }

    private fun parseLessons(lines: List<String>, dayOfWeek: Int): List<Lesson> {
        return lines.mapNotNull { line ->
            val match = LESSON.matchEntire(line) ?: return@mapNotNull null
            val period = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val subject = match.groupValues[2].trim()
            if (subject.isBlank()) return@mapNotNull null
            val room = match.groupValues[3].trim()
            Lesson(dayOfWeek = dayOfWeek, period = period, subject = subject, room = room)
        }
    }

    private fun List<String>.intAfter(prefix: String): Int? =
        firstOrNull { it.startsWith(prefix) }?.removePrefix(prefix)?.toIntOrNull()

    private val BELL = Regex("""^(\d+)\s+(\d{2}:\d{2})-(\d{2}:\d{2})$""")
    private val LESSON = Regex("""^(\d+)\s+([^|]+?)(?:\s+\|\s+(.*))?$""")
}
