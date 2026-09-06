package com.example.firstandroidap.data

import java.time.LocalTime

data class BellPeriod(val start: String, val end: String)

data class SpecialBreak(val afterPeriod: Int, val minutes: Int)

data class BellRecipe(
    val firstStart: String = "08:00",
    val lessonMinutes: Int = 45,
    val breakMinutes: Int = 10,
    val specialBreaks: List<SpecialBreak> = emptyList(),
) {
    fun build(): BellSchedule {
        var cursor = parseHm(firstStart) ?: LocalTime.of(8, 0)
        val special = specialBreaks.associate { it.afterPeriod to it.minutes }
        val periods = (1..SchoolCatalog.PERIODS).map { period ->
            val start = cursor
            val end = start.plusMinutes(lessonMinutes.toLong())
            if (period < SchoolCatalog.PERIODS) {
                val gap = special[period] ?: breakMinutes
                cursor = end.plusMinutes(gap.toLong())
            }
            BellPeriod(formatHm(start), formatHm(end))
        }
        return BellSchedule(periods)
    }

    companion object {
        fun standard(): BellRecipe = BellRecipe(
            firstStart = "08:00",
            lessonMinutes = 45,
            breakMinutes = 10,
            specialBreaks = listOf(SpecialBreak(afterPeriod = 3, minutes = 20)),
        )
    }
}

data class BellSchedule(val periods: List<BellPeriod>) {
    fun of(period: Int): BellPeriod =
        periods.getOrNull(period - 1) ?: BellRecipe.standard().build().of(period)

    fun replacing(period: Int, start: String? = null, end: String? = null): BellSchedule {
        val filled = (1..SchoolCatalog.PERIODS).map { of(it) }.toMutableList()
        val index = (period - 1).coerceIn(0, SchoolCatalog.PERIODS - 1)
        val current = filled[index]
        filled[index] = BellPeriod(start ?: current.start, end ?: current.end)
        return BellSchedule(filled)
    }

    fun encode(): String = periods.joinToString(";") { "${it.start}-${it.end}" }

    companion object {
        fun defaults(): BellSchedule = BellRecipe.standard().build()

        fun decode(raw: String?): BellSchedule = decodeOrNull(raw) ?: defaults()

        fun decodeOrNull(raw: String?): BellSchedule? {
            if (raw.isNullOrBlank()) return null
            val parts = raw.split(';')
            if (parts.size != SchoolCatalog.PERIODS) return null
            val parsed = parts.map { token ->
                val dash = token.indexOf('-')
                if (dash <= 0) return null
                val start = token.substring(0, dash)
                val end = token.substring(dash + 1)
                if (!isHm(start) || !isHm(end)) return null
                BellPeriod(start, end)
            }
            return BellSchedule(parsed)
        }

        private fun isHm(value: String): Boolean = value.matches(HM)

        private val HM = Regex("""\d{2}:\d{2}""")
    }
}

data class WeekBells(
    val week: BellSchedule = BellSchedule.defaults(),
    val overrides: Map<Int, BellSchedule> = emptyMap(),
    val setupDone: Boolean = false,
) {
    fun forDay(dayOfWeek: Int): BellSchedule {
        val day = schoolDay(dayOfWeek)
        return overrides[day] ?: week
    }

    fun isCustom(dayOfWeek: Int): Boolean = overrides.containsKey(schoolDay(dayOfWeek))

    fun withWeek(schedule: BellSchedule): WeekBells =
        copy(week = schedule, overrides = emptyMap(), setupDone = true)

    fun withDay(dayOfWeek: Int, schedule: BellSchedule): WeekBells {
        val day = schoolDay(dayOfWeek)
        return copy(overrides = overrides + (day to schedule), setupDone = true)
    }

    fun markedSetup(): WeekBells = copy(setupDone = true)

    fun encodeDays(): String =
        overrides.entries.sortedBy { it.key }
            .joinToString("|") { "${it.key}:${it.value.encode()}" }

    companion object {
        fun decodeDays(raw: String?): Map<Int, BellSchedule> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split('|').mapNotNull { token ->
                val colon = token.indexOf(':')
                if (colon <= 0) return@mapNotNull null
                val day = token.substring(0, colon).toIntOrNull() ?: return@mapNotNull null
                if (day !in 1..6) return@mapNotNull null
                val schedule = BellSchedule.decodeOrNull(token.substring(colon + 1))
                    ?: return@mapNotNull null
                day to schedule
            }.toMap()
        }

        private fun schoolDay(dayOfWeek: Int): Int =
            if (dayOfWeek == 7) 1 else dayOfWeek.coerceIn(1, 6)
    }
}

fun parseHm(value: String): LocalTime? {
    val parts = value.split(':')
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return try {
        LocalTime.of(hour, minute)
    } catch (_: Exception) {
        null
    }
}

fun formatHm(time: LocalTime): String = "%02d:%02d".format(time.hour, time.minute)
