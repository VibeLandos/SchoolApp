package com.example.firstandroidap.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.firstandroidap.R

enum class ThemeMode(val prefValue: String, @StringRes val titleRes: Int) {
    Cover("cover", R.string.theme_cover),
    System("system", R.string.theme_system),
    Dark("dark", R.string.theme_dark),
    ;

    companion object {
        fun fromPref(value: String?): ThemeMode =
            entries.find { it.prefValue == value } ?: Cover
    }
}

@Immutable
data class DiaryPalette(
    val cover: Color,
    val coverDeep: Color,
    val gold: Color,
    val onGold: Color,
    val paper: Color,
    val paperLine: Color,
    val ink: Color,
    val faintInk: Color,
    val marginRed: Color,
    val homeworkInk: Color,
    val darkChrome: Boolean,
)

val CoverPalette = DiaryPalette(
    cover = CoverBurgundy,
    coverDeep = CoverDeep,
    gold = CoverGold,
    onGold = CoverDeep,
    paper = Paper,
    paperLine = PaperLine,
    ink = Ink,
    faintInk = FaintInk,
    marginRed = MarginRed,
    homeworkInk = HomeworkInk,
    darkChrome = true,
)

val NightPalette = DiaryPalette(
    cover = NightCover,
    coverDeep = NightCoverDeep,
    gold = CoverGold,
    onGold = NightCoverDeep,
    paper = NightPaper,
    paperLine = NightPaperLine,
    ink = NightInk,
    faintInk = NightFaintInk,
    marginRed = NightMargin,
    homeworkInk = NightHomework,
    darkChrome = true,
)

val LocalDiaryPalette = staticCompositionLocalOf { CoverPalette }

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

private val NightColors = darkColorScheme(
    primary = CoverGold,
    onPrimary = NightCoverDeep,
    secondary = NightHomework,
    onSecondary = NightCoverDeep,
    background = NightCoverDeep,
    onBackground = NightInk,
    surface = NightPaper,
    onSurface = NightInk,
    surfaceVariant = Color(0xFF3A3328),
    onSurfaceVariant = NightFaintInk,
    outline = NightPaperLine,
)

private fun paletteFromScheme(scheme: ColorScheme, dark: Boolean) = DiaryPalette(
    cover = scheme.primary,
    coverDeep = scheme.primaryContainer,
    gold = scheme.onPrimary,
    onGold = scheme.primary,
    paper = scheme.surface,
    paperLine = scheme.outline,
    ink = scheme.onSurface,
    faintInk = scheme.onSurfaceVariant,
    marginRed = scheme.error,
    homeworkInk = scheme.secondary,
    darkChrome = dark,
)

@Composable
fun DiaryTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val (scheme, palette) = when (mode) {
        ThemeMode.Cover -> LightColors to CoverPalette
        ThemeMode.Dark -> NightColors to NightPalette
        ThemeMode.System -> {
            val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            val colors = when {
                dynamic && systemDark -> dynamicDarkColorScheme(context)
                dynamic -> dynamicLightColorScheme(context)
                systemDark -> NightColors
                else -> LightColors
            }
            val page = when {
                dynamic -> paletteFromScheme(colors, systemDark)
                systemDark -> NightPalette
                else -> CoverPalette
            }
            colors to page
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = palette.coverDeep.toArgb()
            window.navigationBarColor = palette.coverDeep.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !palette.darkChrome
        }
    }

    CompositionLocalProvider(LocalDiaryPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content,
        )
    }
}
