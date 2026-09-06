package com.example.firstandroidap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import com.example.firstandroidap.ui.SchoolApp
import com.example.firstandroidap.ui.theme.CoverDeep
import com.example.firstandroidap.ui.theme.DiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(CoverDeep.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(CoverDeep.toArgb()),
        )
        val settings = (application as SchoolApplication).themeSettings
        setContent {
            val mode by settings.mode.collectAsState()
            DiaryTheme(mode = mode) {
                SchoolApp(
                    themeMode = mode,
                    onThemeMode = settings::setMode,
                )
            }
        }
    }
}
