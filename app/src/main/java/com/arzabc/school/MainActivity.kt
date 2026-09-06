package com.arzabc.school

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import com.arzabc.school.data.ThemeSettings
import com.arzabc.school.ui.SchoolApp
import com.arzabc.school.ui.theme.CoverDeep
import com.arzabc.school.ui.theme.DiaryTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ThemeSettings.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(CoverDeep.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(CoverDeep.toArgb()),
        )
        val settings = (application as SchoolApplication).themeSettings
        setContent {
            val mode by settings.mode.collectAsState()
            val language by settings.language.collectAsState()
            DiaryTheme(mode = mode) {
                SchoolApp(
                    themeMode = mode,
                    onThemeMode = settings::setMode,
                    language = language,
                    onLanguage = { next ->
                        if (next != language) {
                            settings.setLanguage(next)
                            recreate()
                        }
                    },
                )
            }
        }
    }
}
