package com.arzabc.school.data

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import com.arzabc.school.R
import com.arzabc.school.ui.theme.Appearance
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.ThemeBrightness
import com.arzabc.school.ui.theme.UiStyle
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val prefValue: String, val locale: Locale, @StringRes val titleRes: Int) {
    Russian("ru", Locale("ru"), R.string.language_russian),
    English("en", Locale.ENGLISH, R.string.language_english),
    ;

    companion object {
        fun fromPref(value: String?): AppLanguage =
            entries.find { it.prefValue == value } ?: Russian
    }
}

class ThemeSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _appearance = MutableStateFlow(loadAppearance())
    val appearance: StateFlow<Appearance> = _appearance.asStateFlow()
    private val _language = MutableStateFlow(AppLanguage.fromPref(prefs.getString(KEY_LANGUAGE, null)))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _weekBells = MutableStateFlow(loadWeekBells())
    val weekBells: StateFlow<WeekBells> = _weekBells.asStateFlow()
    private val _schoolDays = MutableStateFlow(loadSchoolDays())
    val schoolDays: StateFlow<Int> = _schoolDays.asStateFlow()
    private val _schoolYear = MutableStateFlow(loadSchoolYear())
    val schoolYear: StateFlow<SchoolYear> = _schoolYear.asStateFlow()

    fun setStyle(style: UiStyle) {
        write(_appearance.value.copy(style = style))
    }

    fun setBrightness(brightness: ThemeBrightness) {
        write(_appearance.value.copy(brightness = brightness))
    }

    fun setSeed(seed: ColorSeed) {
        write(_appearance.value.copy(style = UiStyle.Material, seed = seed))
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.prefValue).apply()
        _language.value = language
    }

    fun setWeekBells(value: WeekBells) {
        prefs.edit()
            .putString(KEY_BELLS, value.week.encode())
            .putString(KEY_BELLS_DAYS, value.encodeDays())
            .putBoolean(KEY_BELLS_SETUP, value.setupDone)
            .apply()
        _weekBells.value = value
    }

    fun setSchoolDays(days: Int) {
        val value = days.coerceIn(5, 6)
        prefs.edit().putInt(KEY_SCHOOL_DAYS, value).apply()
        _schoolDays.value = value
    }

    fun setSchoolYear(value: SchoolYear) {
        val year = value.normalized()
        prefs.edit()
            .putLong(KEY_YEAR_START, year.start.toEpochDay())
            .putLong(KEY_YEAR_END, year.end.toEpochDay())
            .apply()
        _schoolYear.value = year
    }

    private fun write(next: Appearance) {
        prefs.edit()
            .putString(KEY_STYLE, next.style.prefValue)
            .putString(KEY_BRIGHTNESS, next.brightness.prefValue)
            .putString(KEY_SEED, next.seed.prefValue)
            .apply()
        _appearance.value = next
    }

    private fun loadSchoolDays(): Int =
        prefs.getInt(KEY_SCHOOL_DAYS, 6).coerceIn(5, 6)

    private fun loadSchoolYear(): SchoolYear {
        val startDay = prefs.getLong(KEY_YEAR_START, Long.MIN_VALUE)
        val endDay = prefs.getLong(KEY_YEAR_END, Long.MIN_VALUE)
        if (startDay == Long.MIN_VALUE || endDay == Long.MIN_VALUE) {
            return SchoolYear.academicFor()
        }
        return SchoolYear(
            start = LocalDate.ofEpochDay(startDay),
            end = LocalDate.ofEpochDay(endDay),
        ).normalized()
    }

    private fun loadAppearance(): Appearance {
        val storedStyle = prefs.getString(KEY_STYLE, null)
        if (storedStyle != null) {
            return Appearance(
                style = UiStyle.fromPref(storedStyle),
                brightness = ThemeBrightness.fromPref(prefs.getString(KEY_BRIGHTNESS, null)),
                seed = ColorSeed.fromPref(prefs.getString(KEY_SEED, null)),
            )
        }
        return when (prefs.getString(KEY_MODE, null)) {
            "dark" -> Appearance(UiStyle.Material, ThemeBrightness.Dark, ColorSeed.Indigo)
            "system" -> Appearance(UiStyle.Material, ThemeBrightness.System, ColorSeed.Wallpaper)
            else -> Appearance(UiStyle.Material, ThemeBrightness.Light, ColorSeed.Indigo)
        }
    }

    private fun loadWeekBells(): WeekBells {
        val raw = prefs.getString(KEY_BELLS, null)
        return WeekBells(
            week = BellSchedule.decode(raw),
            overrides = WeekBells.decodeDays(prefs.getString(KEY_BELLS_DAYS, null)),
            setupDone = prefs.getBoolean(KEY_BELLS_SETUP, false),
        )
    }

    companion object {
        private const val PREFS = "diary_settings"
        private const val KEY_MODE = "theme_mode"
        private const val KEY_STYLE = "ui_style"
        private const val KEY_BRIGHTNESS = "theme_brightness"
        private const val KEY_SEED = "color_seed"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_BELLS = "bells"
        private const val KEY_BELLS_DAYS = "bells_days"
        private const val KEY_BELLS_SETUP = "bells_setup"
        private const val KEY_SCHOOL_DAYS = "school_days"
        private const val KEY_YEAR_START = "school_year_start"
        private const val KEY_YEAR_END = "school_year_end"

        fun wrapContext(base: Context): Context {
            val prefs = base.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val language = AppLanguage.fromPref(prefs.getString(KEY_LANGUAGE, null))
            val locale = language.locale
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            config.setLocales(LocaleList(locale))
            return base.createConfigurationContext(config)
        }
    }
}
