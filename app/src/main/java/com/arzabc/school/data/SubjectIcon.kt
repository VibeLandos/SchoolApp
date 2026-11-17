package com.arzabc.school.data

import androidx.annotation.DrawableRes
import com.arzabc.school.R

enum class SubjectIcon(
    val key: String,
    val title: String,
    @DrawableRes val iconRes: Int,
) {
    ABACUS("abacus", "Математика / Алгебра", R.drawable.ic_subject_abacus),
    ATOM("atom", "Физика", R.drawable.ic_subject_atom),
    COMPUTER("computer", "Информатика", R.drawable.ic_subject_computer),
    BOOK("book", "Русский язык / Литература", R.drawable.ic_subject_book),
    GLOBE("globe", "География / История", R.drawable.ic_subject_globe),
    LANGUAGE("language", "Иностранные языки", R.drawable.ic_subject_language),
    FLASK("flask", "Химия / Биология", R.drawable.ic_subject_flask),
    BALL("ball", "Физкультура", R.drawable.ic_subject_ball),
    PEN("pen", "Письмо / Черчение", R.drawable.ic_subject_pen),
    PALETTE("palette", "ИЗО / Рисование", R.drawable.ic_subject_palette),
    MUSIC("music", "Музыка", R.drawable.ic_subject_music),
    DIARY("diary", "Общее / Дневник", R.drawable.ic_subject_diary);

    companion object {
        fun fromKey(key: String?): SubjectIcon {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: DIARY
        }

        fun resolve(subject: String, customMap: Map<String, String> = emptyMap()): SubjectIcon {
            val trimmed = subject.trim()
            val custom = customMap[trimmed]
            if (custom != null) {
                return fromKey(custom)
            }
            return defaultFor(trimmed)
        }

        fun defaultFor(subject: String): SubjectIcon {
            val s = subject.lowercase().trim()
            return when {
                s.contains("мат") || s.contains("алгебр") || s.contains("геом") || s.contains("math") -> ABACUS
                s.contains("физик") || s.contains("physics") -> ATOM
                s.contains("информ") || s.contains("компьют") || s.contains("програм") || s.contains("it") || s.contains("ит") -> COMPUTER
                s.contains("рус") || s.contains("лит") || s.contains("чтен") || s.contains("родн") -> BOOK
                s.contains("истор") || s.contains("геогр") || s.contains("обществ") || s.contains("окруж") || s.contains("краев") -> GLOBE
                s.contains("англ") || s.contains("нем") || s.contains("франц") || s.contains("испан") || s.contains("китай") || s.contains("язык") || s.contains("ин.яз") || s.contains("english") -> LANGUAGE
                s.contains("хим") || s.contains("биол") || s.contains("естеств") || s.contains("экол") || s.contains("анатом") -> FLASK
                s.contains("физ-р") || s.contains("физр") || s.contains("спорт") || s.contains("плаван") || s.contains("басс") || s.contains("лфк") -> BALL
                s.contains("черч") || s.contains("технолог") || s.contains("труд") || s.contains("письм") || s.contains("проект") -> PEN
                s.contains("изо") || s.contains("рисова") || s.contains("живопис") || s.contains("искусств") || s.contains("дизайн") -> PALETTE
                s.contains("музык") || s.contains("хор") || s.contains("вокал") || s.contains("сольфедж") -> MUSIC
                else -> DIARY
            }
        }
    }
}
