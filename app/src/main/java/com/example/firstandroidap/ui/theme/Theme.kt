package com.example.firstandroidap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = CoverBurgundy,
    onPrimary = CoverGold,
    secondary = HomeworkInk,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE9E0C8),
    onSurfaceVariant = Ink,
    outline = PaperLine,
)

private val DarkColors = darkColorScheme(
    primary = CoverGold,
    onPrimary = CoverDeep,
    secondary = Color(0xFF9CBCDB),
    onSecondary = CoverDeep,
    background = CoverDeep,
    onBackground = Paper,
    surface = Color(0xFF5A2830),
    onSurface = Paper,
)

@Composable
fun DiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
