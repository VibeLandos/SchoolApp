package com.arzabc.school.data

import java.time.LocalDate

data class SchoolYear(val start: LocalDate, val end: LocalDate) {
    fun normalized(): SchoolYear = if (end.isBefore(start)) SchoolYear(end, start) else this

    companion object {
        fun academicFor(today: LocalDate = LocalDate.now()): SchoolYear {
            val startYear = if (today.monthValue >= 9) today.year else today.year - 1
            return SchoolYear(
                start = LocalDate.of(startYear, 9, 1),
                end = LocalDate.of(startYear + 1, 5, 25),
            )
        }
    }
}
