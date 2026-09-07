package com.arzabc.school.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.arzabc.school.R

enum class UiStyle(val prefValue: String, @StringRes val titleRes: Int) {
    Material("material", R.string.theme_material),
    Glass("glass", R.string.theme_glass),
    ;

    companion object {
        fun fromPref(value: String?): UiStyle =
            entries.find { it.prefValue == value } ?: Material
    }
}

enum class ThemeBrightness(val prefValue: String, @StringRes val titleRes: Int) {
    Light("light", R.string.theme_light),
    Dark("dark", R.string.theme_dark),
    System("system", R.string.theme_follow_system),
    ;

    companion object {
        fun fromPref(value: String?): ThemeBrightness =
            entries.find { it.prefValue == value } ?: Light
    }
}

data class Appearance(
    val style: UiStyle = UiStyle.Material,
    val brightness: ThemeBrightness = ThemeBrightness.Light,
    val seed: ColorSeed = ColorSeed.Indigo,
)

val LocalUiStyle = staticCompositionLocalOf { UiStyle.Material }
val LocalGlassTokens = staticCompositionLocalOf { GlassLight }

@Composable
fun DiaryTheme(
    appearance: Appearance,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val dark = when (appearance.brightness) {
        ThemeBrightness.Light -> false
        ThemeBrightness.Dark -> true
        ThemeBrightness.System -> systemDark
    }
    val glass = appearance.style == UiStyle.Glass
    val glassTokens = if (dark) GlassDark else GlassLight
    val scheme = if (!glass &&
        appearance.seed == ColorSeed.Wallpaper &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        seedColorScheme(
            seed = if (glass) ColorSeed.Indigo else appearance.seed,
            dark = dark,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        val statusDarkIcons = scheme.surface.luminance() > 0.45f
        SideEffect {
            val window = (view.context as Activity).window
            val bar = if (glass) {
                android.graphics.Color.TRANSPARENT
            } else {
                scheme.surface.toArgb()
            }
            window.statusBarColor = bar
            window.navigationBarColor = if (glass) {
                android.graphics.Color.TRANSPARENT
            } else {
                scheme.surfaceContainer.toArgb()
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = statusDarkIcons
                isAppearanceLightNavigationBars = statusDarkIcons
            }
        }
    }

    CompositionLocalProvider(
        LocalUiStyle provides appearance.style,
        LocalGlassTokens provides glassTokens,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content,
        )
    }
}
