package com.arzabc.school.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.data.SchoolYear
import com.arzabc.school.ui.components.BlobShape
import com.arzabc.school.ui.theme.Appearance
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.ThemeBrightness
import com.arzabc.school.ui.theme.UiStyle

@Composable
fun ProfileScreen(
    appearance: Appearance,
    onStyle: (UiStyle) -> Unit,
    onBrightness: (ThemeBrightness) -> Unit,
    onSeed: (ColorSeed) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    schoolDays: Int,
    onSchoolDays: (Int) -> Unit,
    schoolYear: SchoolYear,
    onOpenBells: () -> Unit,
    onOpenSchoolYear: () -> Unit,
    onShareDay: () -> Unit,
    onShareWeek: () -> Unit,
    onImportSchedule: () -> Unit,
    onCheckUpdate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
        ) {
            Text(
                text = "Настройки и профиль",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Профиль",
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // User Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(BlobShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "А",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Школьный дневник",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Версия 2.0",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // Section: Расписание и звонки
            item {
                Text(
                    text = "Расписание",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 2.dp),
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Notifications,
                    title = "Расписание звонков",
                    subtitle = "Настройка уроков и перемен",
                    onClick = onOpenBells,
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Учебная неделя",
                    subtitle = if (schoolDays == 5) "5 дней · пн–пт" else "6 дней · пн–сб",
                    onClick = { onSchoolDays(if (schoolDays == 5) 6 else 5) },
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.DateRange,
                    title = "Учебный год",
                    subtitle = "${schoolYear.start.year}–${schoolYear.end.year} уч. год",
                    onClick = onOpenSchoolYear,
                )
            }

            // Section: Передача расписания
            item {
                Text(
                    text = "Обмен расписанием",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 2.dp),
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Share,
                    title = "Поделиться днем",
                    subtitle = "Отправить уроки выбранного дня",
                    onClick = onShareDay,
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Share,
                    title = "Поделиться неделей",
                    subtitle = "Отправить текст расписания",
                    onClick = onShareWeek,
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.FileDownload,
                    title = "Вставить расписание",
                    subtitle = "Импортировать день или неделю",
                    onClick = onImportSchedule,
                )
            }

            // Section: Приложение
            item {
                Text(
                    text = "Оформление и система",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 2.dp),
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Palette,
                    title = "Тема оформления",
                    subtitle = when (appearance.brightness) {
                        ThemeBrightness.Light -> "Светлая"
                        ThemeBrightness.Dark -> "Тёмная"
                        ThemeBrightness.System -> "Как в системе"
                    },
                    onClick = {
                        val next = when (appearance.brightness) {
                            ThemeBrightness.Light -> ThemeBrightness.Dark
                            ThemeBrightness.Dark -> ThemeBrightness.System
                            ThemeBrightness.System -> ThemeBrightness.Light
                        }
                        onBrightness(next)
                    },
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Palette,
                    title = "Цветовая гамма",
                    subtitle = when (appearance.seed) {
                        ColorSeed.Purple -> "Фиолетовая (MD3)"
                        ColorSeed.Indigo -> "Индиго"
                        ColorSeed.Teal -> "Морская волна"
                        ColorSeed.Coral -> "Коралловая"
                        ColorSeed.Wallpaper -> "Цвета обоев"
                    },
                    onClick = {
                        val next = when (appearance.seed) {
                            ColorSeed.Purple -> ColorSeed.Indigo
                            ColorSeed.Indigo -> ColorSeed.Teal
                            ColorSeed.Teal -> ColorSeed.Coral
                            ColorSeed.Coral -> ColorSeed.Wallpaper
                            ColorSeed.Wallpaper -> ColorSeed.Purple
                        }
                        onSeed(next)
                    },
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.Language,
                    title = "Язык приложения",
                    subtitle = if (language == AppLanguage.Russian) "Русский" else "English",
                    onClick = {
                        onLanguage(if (language == AppLanguage.Russian) AppLanguage.English else AppLanguage.Russian)
                    },
                )
            }

            item {
                SettingsActionCard(
                    icon = Icons.Outlined.SystemUpdate,
                    title = "Проверить обновления",
                    subtitle = "Проверка новых версий на GitHub",
                    onClick = onCheckUpdate,
                )
            }
        }
    }
}

@Composable
private fun SettingsActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp),
        )
    }
}
