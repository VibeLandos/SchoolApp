package com.arzabc.school

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arzabc.school.data.ThemeSettings
import com.arzabc.school.ui.SchoolApp
import com.arzabc.school.ui.theme.DiaryTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ThemeSettings.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        val settings = (application as SchoolApplication).themeSettings
        setContent {
            val appearance by settings.appearance.collectAsState()
            val language by settings.language.collectAsState()
            DiaryTheme(appearance = appearance) {
                SchoolApp(
                    appearance = appearance,
                    onStyle = settings::setStyle,
                    onBrightness = settings::setBrightness,
                    onSeed = settings::setSeed,
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
