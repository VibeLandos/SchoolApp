package com.example.firstandroidap.data

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import com.example.firstandroidap.R
import com.example.firstandroidap.ui.theme.ThemeMode
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
    private val _mode = MutableStateFlow(ThemeMode.fromPref(prefs.getString(KEY_MODE, null)))
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()
    private val _language = MutableStateFlow(AppLanguage.fromPref(prefs.getString(KEY_LANGUAGE, null)))
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _weekBells = MutableStateFlow(loadWeekBells())
    val weekBells: StateFlow<WeekBells> = _weekBells.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.prefValue).apply()
        _mode.value = mode
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
        private const val KEY_LANGUAGE = "language"
        private const val KEY_BELLS = "bells"
        private const val KEY_BELLS_DAYS = "bells_days"
        private const val KEY_BELLS_SETUP = "bells_setup"

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
