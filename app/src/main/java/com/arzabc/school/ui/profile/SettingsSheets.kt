package com.arzabc.school.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.ThemeBrightness

@Composable
private fun Md3SelectionOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    leadingContent: @Composable () -> Unit,
) {
    val containerBg = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            leadingContent()
        }

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        RadioButton(
            selected = selected,
            onClick = null,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionSheet(
    current: ThemeBrightness,
    onSelect: (ThemeBrightness) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Тема оформления",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            Md3SelectionOption(
                title = "Светлая",
                subtitle = "Светлое оформление интерфейса",
                selected = current == ThemeBrightness.Light,
                onSelect = {
                    onSelect(ThemeBrightness.Light)
                    onDismiss()
                },
                leadingContent = {
                    Icon(
                        Icons.Outlined.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            Spacer(Modifier.height(6.dp))
            Md3SelectionOption(
                title = "Тёмная",
                subtitle = "Тёмное оформление для экономии батареи и комфорта глаз",
                selected = current == ThemeBrightness.Dark,
                onSelect = {
                    onSelect(ThemeBrightness.Dark)
                    onDismiss()
                },
                leadingContent = {
                    Icon(
                        Icons.Outlined.DarkMode,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            Spacer(Modifier.height(6.dp))
            Md3SelectionOption(
                title = "Как в системе",
                subtitle = "Переключается вслед за системой Android",
                selected = current == ThemeBrightness.System,
                onSelect = {
                    onSelect(ThemeBrightness.System)
                    onDismiss()
                },
                leadingContent = {
                    Icon(
                        Icons.Outlined.SettingsBrightness,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}

private data class ColorSeedItem(
    val seed: ColorSeed,
    val title: String,
    val subtitle: String,
    val color: Color,
    val isDynamic: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorSeedSelectionSheet(
    current: ColorSeed,
    onSelect: (ColorSeed) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = listOf(
        ColorSeedItem(
            seed = ColorSeed.Wallpaper,
            title = "Цвета обоев",
            subtitle = "Палитра Material You из обоев устройства",
            color = Color(0xFF5B7CFA),
            isDynamic = true,
        ),
        ColorSeedItem(
            seed = ColorSeed.Purple,
            title = "Фиолетовая",
            subtitle = "Классический MD3",
            color = Color(0xFF6750A4),
        ),
        ColorSeedItem(
            seed = ColorSeed.Indigo,
            title = "Индиго",
            subtitle = "Глубокий синий оттенок",
            color = Color(0xFF0061A4),
        ),
        ColorSeedItem(
            seed = ColorSeed.Teal,
            title = "Морская волна",
            subtitle = "Бирюзовый оттенок",
            color = Color(0xFF006A60),
        ),
        ColorSeedItem(
            seed = ColorSeed.Coral,
            title = "Коралловая",
            subtitle = "Тёплый кораллово-оранжевый оттенок",
            color = Color(0xFF9C4235),
        ),
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Цветовая гамма",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Выберите акцентный цвет интерфейса",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            options.forEach { opt ->
                Md3SelectionOption(
                    title = opt.title,
                    subtitle = opt.subtitle,
                    selected = current == opt.seed,
                    onSelect = {
                        onSelect(opt.seed)
                        onDismiss()
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(opt.color),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (opt.isDynamic) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    },
                )
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolDaysSelectionSheet(
    current: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Учебная неделя",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            Md3SelectionOption(
                title = "5 дней",
                subtitle = "С понедельника по пятницу, суббота выходной",
                selected = current == 5,
                onSelect = {
                    onSelect(5)
                    onDismiss()
                },
                leadingContent = {
                    Text(
                        text = "5",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            Spacer(Modifier.height(6.dp))
            Md3SelectionOption(
                title = "6 дней",
                subtitle = "С понедельника по субботу",
                selected = current == 6,
                onSelect = {
                    onSelect(6)
                    onDismiss()
                },
                leadingContent = {
                    Text(
                        text = "6",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionSheet(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text(
                text = "Язык интерфейса",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))

            Md3SelectionOption(
                title = "Русский",
                subtitle = "Русский язык",
                selected = current == AppLanguage.Russian,
                onSelect = {
                    onSelect(AppLanguage.Russian)
                    onDismiss()
                },
                leadingContent = {
                    Text(
                        text = "RU",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            Spacer(Modifier.height(6.dp))
            Md3SelectionOption(
                title = "English",
                subtitle = "English interface",
                selected = current == AppLanguage.English,
                onSelect = {
                    onSelect(AppLanguage.English)
                    onDismiss()
                },
                leadingContent = {
                    Text(
                        text = "EN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}
