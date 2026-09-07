package com.arzabc.school.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.LocalGlassTokens
import com.arzabc.school.ui.theme.LocalUiStyle
import com.arzabc.school.ui.theme.UiStyle
import com.arzabc.school.ui.theme.glassSurface
import com.arzabc.school.ui.theme.swatch

@Composable
fun isGlassStyle(): Boolean = LocalUiStyle.current == UiStyle.Glass

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenMenu) {
                Icon(
                    Icons.Outlined.Menu,
                    contentDescription = stringResource(R.string.cd_open_menu),
                    tint = if (glass) tokens.text else scheme.onSurface,
                )
            }
            Column(Modifier.weight(1f)) {
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        color = if (glass) tokens.accent else scheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp,
                    )
                }
                Text(
                    text = title,
                    color = if (glass) tokens.text else scheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            actions()
        }
    }
}

@Composable
fun DiaryCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(if (glass) 22.dp else 16.dp)
    val bg = when {
        highlighted && !glass -> scheme.primaryContainer
        glass -> tokens.card
        else -> scheme.surfaceContainerLow
    }
    val border = when {
        highlighted && glass -> tokens.accent.copy(alpha = 0.35f)
        glass -> tokens.border
        else -> Color.Transparent
    }
    val contentColor = when {
        glass -> tokens.text
        highlighted -> scheme.onPrimaryContainer
        else -> scheme.onSurface
    }
    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg, shape)
            .then(if (glass || highlighted) Modifier.border(1.dp, border, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun PeriodBadge(period: Int, highlighted: Boolean = false) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val bg = when {
        highlighted && !glass -> scheme.primary
        highlighted && glass -> tokens.accent
        glass -> tokens.subtle
        else -> scheme.secondaryContainer
    }
    val fg = when {
        highlighted && !glass -> scheme.onPrimary
        highlighted && glass -> Color.White
        glass -> tokens.text
        else -> scheme.onSecondaryContainer
    }
    Box(
        Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = period.toString(),
            color = fg,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}

@Composable
fun SeedSwatches(
    selected: ColorSeed,
    onSelect: (ColorSeed) -> Unit,
    enabled: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
    ) {
        ColorSeed.entries.forEach { seed ->
            val on = selected == seed && enabled
            Box(
                Modifier
                    .size(if (on) 36.dp else 32.dp)
                    .clip(CircleShape)
                    .background(seed.swatch())
                    .then(
                        if (on) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        else Modifier,
                    )
                    .clickable(enabled = enabled) { onSelect(seed) },
            )
        }
    }
}

@Composable
fun SpanProgressBar(progress: Float) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = if (glass) tokens.accent else MaterialTheme.colorScheme.primary,
        trackColor = if (glass) tokens.subtle else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
    )
}

@Composable
fun LessonProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 48.dp,
    stroke: Dp = 4.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val active = if (glass) tokens.accent else scheme.primary
    val track = if (glass) tokens.subtle else scheme.outline.copy(alpha = 0.35f)
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
            )
            if (clamped > 0f) {
                drawArc(
                    color = active,
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = strokePx,
                        cap = if (clamped >= 0.999f) StrokeCap.Butt else StrokeCap.Round,
                    ),
                )
            }
        }
        content()
    }
}
