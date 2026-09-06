package com.arzabc.school.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.arzabc.school.R

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

/** Ночь: та же тетрадь, темнее кожа обложки, бумага чуть приглушена — не инверсия. */
val NightPalette = DiaryPalette(
    cover = NightCover,
    coverDeep = NightCoverDeep,
    gold = NightGold,
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

private fun foilOn(cover: Color): Color =
    if (cover.luminance() > 0.45f) CoverDeep else CoverGold

private fun stainLeather(base: DiaryPalette, stain: Color, amount: Float = 0.28f): DiaryPalette {
    val cover = lerp(base.cover, stain, amount)
    val deep = lerp(base.coverDeep, stain, amount * 0.85f)
    val gold = foilOn(cover)
    return base.copy(
        cover = cover,
        coverDeep = deep,
        gold = gold,
        onGold = if (gold.luminance() > 0.5f) deep else CoverGold,
        darkChrome = cover.luminance() < 0.45f,
    )
}

private fun materialFor(palette: DiaryPalette) = if (palette.paper.luminance() < 0.4f) {
    darkColorScheme(
        primary = palette.cover,
        onPrimary = palette.gold,
        secondary = palette.homeworkInk,
        onSecondary = palette.paper,
        background = palette.cover,
        onBackground = palette.gold,
        surface = palette.paper,
        onSurface = palette.ink,
        surfaceVariant = palette.paper,
        onSurfaceVariant = palette.faintInk,
        outline = palette.paperLine,
        error = palette.marginRed,
        onError = palette.paper,
    )
} else {
    lightColorScheme(
        primary = palette.cover,
        onPrimary = palette.gold,
        secondary = palette.homeworkInk,
        onSecondary = Color.White,
        background = palette.cover,
        onBackground = palette.gold,
        surface = palette.paper,
        onSurface = palette.ink,
        surfaceVariant = Color(0xFFE9E0C8),
        onSurfaceVariant = palette.faintInk,
        outline = palette.paperLine,
        error = palette.marginRed,
        onError = Color.White,
    )
}

@Composable
fun DiaryTheme(
    mode: ThemeMode,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val palette = when (mode) {
        ThemeMode.Cover -> CoverPalette
        ThemeMode.Dark -> NightPalette
        ThemeMode.System -> {
            val notebook = if (systemDark) NightPalette else CoverPalette
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val dynamic = if (systemDark) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
                stainLeather(notebook, dynamic.primary)
            } else {
                notebook
            }
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
            colorScheme = materialFor(palette),
            typography = Typography,
            content = content,
        )
    }
}
