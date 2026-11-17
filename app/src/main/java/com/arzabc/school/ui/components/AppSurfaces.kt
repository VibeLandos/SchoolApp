package com.arzabc.school.ui.components

import com.arzabc.school.data.SubjectIcon
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import java.time.LocalDate
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
fun CenteredChoiceDialog(
    title: String,
    onDismiss: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(if (glass) 28.dp else 24.dp)
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (glass) {
                        Modifier.glassSurface(tokens, 28.dp)
                    } else {
                        Modifier
                            .clip(shape)
                            .background(scheme.surfaceContainerHigh, shape)
                    },
                )
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = if (glass) tokens.text else scheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    textAlign = TextAlign.Center,
                    color = if (glass) tokens.textSecondary else scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun WeekDayChips(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    today: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = false,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        weekDates.forEach { date ->
            val selected = date == selectedDate
            val shape = RoundedCornerShape(if (glass) 14.dp else 12.dp)
            val bg = when {
                selected && glass && tokens.darkChrome -> Color.White.copy(alpha = 0.22f)
                selected && glass -> Color.White.copy(alpha = 0.92f)
                selected -> scheme.secondaryContainer
                glass -> tokens.subtle
                else -> scheme.surfaceContainerLow
            }
            val fg = when {
                selected && glass && tokens.darkChrome -> Color.White
                selected && glass -> Color(0xFF111111)
                selected -> scheme.onSecondaryContainer
                glass -> tokens.textSecondary
                else -> scheme.onSurfaceVariant
            }
            val todayChip = date == today
            val accent = if (glass) tokens.accent else scheme.primary
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(if (showDate) 52.dp else 40.dp)
                    .clip(shape)
                    .background(bg)
                    .border(
                        1.dp,
                        when {
                            selected && !glass -> scheme.primary
                            glass -> tokens.border
                            else -> scheme.outlineVariant
                        },
                        shape,
                    )
                    .clickable { onSelectDate(date) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = Dates.weekdayShort(date).uppercase(),
                    color = if (todayChip && !selected) accent else fg,
                    fontSize = if (showDate) 11.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (showDate) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (todayChip && !selected) accent else fg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiaryCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
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
            .then(
                when {
                    onLongClick != null -> Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick,
                    )
                    onClick != null -> Modifier.clickable(onClick = onClick)
                    else -> Modifier
                },
            )
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

val BlobShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(0.42f * w, 0f)
    cubicTo(0.75f * w, 0f, w, 0.20f * h, w, 0.45f * h)
    cubicTo(w, 0.70f * h, 0.85f * w, h, 0.62f * w, h)
    cubicTo(0.38f * w, h, 0f, 0.85f * h, 0f, 0.60f * h)
    cubicTo(0f, 0.35f * h, 0.15f * w, 0f, 0.42f * w, 0f)
    close()
}

@Composable
fun Md3TopHeader(
    subtitle: String,
    title: String,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarText: String = "А",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 24.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(BlobShape)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
fun HeroBannerCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
fun GradeBlobBadge(
    grade: Int,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: TextUnit = 20.sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(BlobShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = grade.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
fun Md3LessonCard(
    time: String,
    subject: String,
    homeworkOrRoom: String,
    grade: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: SubjectIcon = SubjectIcon.defaultFor(subject),
    onIconClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = time,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp),
        )
        SubjectIconBadge(
            icon = icon,
            size = 52.dp,
            iconSize = 28.dp,
            onClick = onIconClick,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp),
        ) {
            Text(
                text = subject,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (homeworkOrRoom.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = homeworkOrRoom,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (grade != null && grade > 0) {
            GradeBlobBadge(grade = grade, size = 48.dp)
        }
    }
}
