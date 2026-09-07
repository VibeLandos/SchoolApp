package com.arzabc.school.ui.theme

import androidx.annotation.StringRes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.arzabc.school.R

enum class ColorSeed(val prefValue: String, @StringRes val titleRes: Int) {
    Wallpaper("wallpaper", R.string.seed_wallpaper),
    Indigo("indigo", R.string.seed_indigo),
    Teal("teal", R.string.seed_teal),
    Coral("coral", R.string.seed_coral),
    Purple("purple", R.string.seed_purple),
    ;

    companion object {
        fun fromPref(value: String?): ColorSeed =
            entries.find { it.prefValue == value } ?: Indigo
    }
}

fun ColorSeed.swatch(): Color = when (this) {
    ColorSeed.Wallpaper -> Color(0xFF5B7CFA)
    ColorSeed.Indigo -> Color(0xFF0061A4)
    ColorSeed.Teal -> Color(0xFF006A60)
    ColorSeed.Coral -> Color(0xFF9C4235)
    ColorSeed.Purple -> Color(0xFF6750A4)
}

internal fun seedColorScheme(seed: ColorSeed, dark: Boolean) = when (seed) {
    ColorSeed.Wallpaper,
    ColorSeed.Indigo,
    -> if (dark) indigoDark else indigoLight
    ColorSeed.Teal -> if (dark) tealDark else tealLight
    ColorSeed.Coral -> if (dark) coralDark else coralLight
    ColorSeed.Purple -> if (dark) purpleDark else purpleLight
}

private val indigoLight = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2E8),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E7EE),
    surfaceContainerHighest = Color(0xFFE1E2E8),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7D0),
    error = Color(0xFFB3261E),
)

private val indigoDark = darkColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF00315B),
    primaryContainer = Color(0xFF004881),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBDC7DC),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3E4759),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFD6BEE4),
    tertiaryContainer = Color(0xFF534060),
    onTertiaryContainer = Color(0xFFF2DAFF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF33353A),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainerLowest = Color(0xFF0C0E13),
    surfaceContainerLow = Color(0xFF191C20),
    surfaceContainer = Color(0xFF1D2024),
    surfaceContainerHigh = Color(0xFF282A2F),
    surfaceContainerHighest = Color(0xFF33353A),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    error = Color(0xFFF2B8B5),
)

private val tealLight = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF73F8E7),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCE8E3),
    onSecondaryContainer = Color(0xFF05201D),
    background = Color(0xFFF5FAF8),
    onBackground = Color(0xFF161D1B),
    surface = Color(0xFFF5FAF8),
    onSurface = Color(0xFF161D1B),
    surfaceVariant = Color(0xFFDDE5E2),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF5F2),
    surfaceContainer = Color(0xFFE9F0ED),
    surfaceContainerHigh = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFDDE5E2),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBFC9C6),
    error = Color(0xFFB3261E),
)

private val tealDark = darkColorScheme(
    primary = Color(0xFF53DBCB),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005048),
    onPrimaryContainer = Color(0xFF73F8E7),
    secondary = Color(0xFFB1CCC5),
    onSecondary = Color(0xFF1C3531),
    secondaryContainer = Color(0xFF334B47),
    onSecondaryContainer = Color(0xFFCCE8E3),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFDFE4E1),
    surface = Color(0xFF0F1513),
    onSurface = Color(0xFFDFE4E1),
    surfaceVariant = Color(0xFF303634),
    onSurfaceVariant = Color(0xFFBFC9C6),
    surfaceContainerLowest = Color(0xFF0A100E),
    surfaceContainerLow = Color(0xFF171D1B),
    surfaceContainer = Color(0xFF1B211F),
    surfaceContainerHigh = Color(0xFF252B29),
    surfaceContainerHighest = Color(0xFF303634),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    error = Color(0xFFFFB4AB),
)

private val coralLight = lightColorScheme(
    primary = Color(0xFF9C4235),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF3E0400),
    secondary = Color(0xFF77574E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4DDDA),
    onSecondaryContainer = Color(0xFF2A1614),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231A17),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231A17),
    surfaceVariant = Color(0xFFEAE0DD),
    onSurfaceVariant = Color(0xFF53433F),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF0ED),
    surfaceContainer = Color(0xFFF6EBE8),
    surfaceContainerHigh = Color(0xFFF0E5E2),
    surfaceContainerHighest = Color(0xFFEAE0DD),
    outline = Color(0xFF85736E),
    outlineVariant = Color(0xFFD8C2BC),
    error = Color(0xFFB3261E),
)

private val coralDark = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    onPrimary = Color(0xFF5F1607),
    primaryContainer = Color(0xFF7D2C20),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFE7BDB2),
    onSecondary = Color(0xFF442A24),
    secondaryContainer = Color(0xFF5A403C),
    onSecondaryContainer = Color(0xFFF4DDDA),
    background = Color(0xFF1A110F),
    onBackground = Color(0xFFF0DFDB),
    surface = Color(0xFF1A110F),
    onSurface = Color(0xFFF0DFDB),
    surfaceVariant = Color(0xFF3D3330),
    onSurfaceVariant = Color(0xFFD8C2BC),
    surfaceContainerLowest = Color(0xFF140C0A),
    surfaceContainerLow = Color(0xFF231A17),
    surfaceContainer = Color(0xFF271E1B),
    surfaceContainerHigh = Color(0xFF322826),
    surfaceContainerHighest = Color(0xFF3D3330),
    outline = Color(0xFFA08C87),
    outlineVariant = Color(0xFF53433F),
    error = Color(0xFFFFB4AB),
)

private val purpleLight = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFDF8FF),
    onBackground = Color(0xFF1D1A20),
    surface = Color(0xFFFDF8FF),
    onSurface = Color(0xFF1D1A20),
    surfaceVariant = Color(0xFFE7E1E9),
    onSurfaceVariant = Color(0xFF4A4450),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F2FA),
    surfaceContainer = Color(0xFFF2ECF4),
    surfaceContainerHigh = Color(0xFFECE6EE),
    surfaceContainerHighest = Color(0xFFE7E1E9),
    outline = Color(0xFF7C7482),
    outlineVariant = Color(0xFFCCC4CF),
    error = Color(0xFFB3261E),
)

private val purpleDark = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCBC2DB),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = Color(0xFF15121A),
    onBackground = Color(0xFFE9E1EA),
    surface = Color(0xFF15121A),
    onSurface = Color(0xFFE9E1EA),
    surfaceVariant = Color(0xFF37343C),
    onSurfaceVariant = Color(0xFFCCC4CF),
    surfaceContainerLowest = Color(0xFF100E14),
    surfaceContainerLow = Color(0xFF1D1A22),
    surfaceContainer = Color(0xFF211E26),
    surfaceContainerHigh = Color(0xFF2C2931),
    surfaceContainerHighest = Color(0xFF37343C),
    outline = Color(0xFF968E9C),
    outlineVariant = Color(0xFF4A4450),
    error = Color(0xFFFFB4AB),
)
