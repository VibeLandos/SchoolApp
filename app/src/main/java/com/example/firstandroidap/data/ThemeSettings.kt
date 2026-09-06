package com.example.firstandroidap.data

import android.content.Context
import com.example.firstandroidap.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(ThemeMode.fromPref(prefs.getString(KEY_MODE, null)))
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.prefValue).apply()
        _mode.value = mode
    }

    private companion object {
        const val PREFS = "diary_settings"
        const val KEY_MODE = "theme_mode"
    }
}
