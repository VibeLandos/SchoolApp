package com.arzabc.school.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class GlassTokens(
    val card: Color,
    val subtle: Color,
    val border: Color,
    val text: Color,
    val textSecondary: Color,
    val accent: Color,
    val tabBar: Color,
    val darkChrome: Boolean,
)

val GlassLight = GlassTokens(
    card = Color(0xC2FFFFFF),
    subtle = Color(0x8AFFFFFF),
    border = Color(0xA6FFFFFF),
    text = Color(0xF012141A),
    textSecondary = Color(0xC23C404C),
    accent = Color(0xFF007AFF),
    tabBar = Color(0xC8FFFFFF),
    darkChrome = false,
)

val GlassDark = GlassTokens(
    card = Color(0xD4181C28),
    subtle = Color(0x33FFFFFF),
    border = Color(0x55FFFFFF),
    text = Color(0xFFFFFFFF),
    textSecondary = Color(0xD6EBEBF5),
    accent = Color(0xFF64D2FF),
    tabBar = Color(0xD4181C28),
    darkChrome = true,
)

fun Modifier.glassSurface(tokens: GlassTokens, radius: Dp = 22.dp): Modifier =
    clip(RoundedCornerShape(radius))
        .background(tokens.card, RoundedCornerShape(radius))
        .border(1.dp, tokens.border, RoundedCornerShape(radius))

@Composable
fun GlassWallpaper(dark: Boolean, modifier: Modifier = Modifier) {
    val base = if (dark) Color(0xFF0A0D1D) else Color(0xFFEEF4FF)
    Box(modifier.fillMaxSize().background(base)) {
        if (dark) {
            Blob(Color(0x8C2997FF), 320.dp, (-40).dp, (-20).dp)
            Blob(Color(0x80DA3EEC), 280.dp, 220.dp, 40.dp)
            Blob(Color(0x995856D6), 340.dp, 40.dp, 280.dp)
            Blob(Color(0x7330D158), 260.dp, 200.dp, 520.dp)
        } else {
            Blob(Color(0xA678C8FF), 300.dp, (-50).dp, (-30).dp)
            Blob(Color(0x8CFFAAC8), 280.dp, 200.dp, 80.dp)
            Blob(Color(0x80B496FF), 320.dp, 20.dp, 340.dp)
            Blob(Color(0x8C82E6D7), 260.dp, 210.dp, 560.dp)
        }
    }
}

@Composable
private fun Blob(color: Color, size: Dp, x: Dp, y: Dp) {
    Box(
        Modifier
            .offset(x, y)
            .size(size)
            .background(
                Brush.radialGradient(listOf(color, Color.Transparent)),
                CircleShape,
            ),
    )
}
