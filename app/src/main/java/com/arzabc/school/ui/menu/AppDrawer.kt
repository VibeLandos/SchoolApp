package com.arzabc.school.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arzabc.school.R
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.ui.components.SeedSwatches
import com.arzabc.school.ui.theme.Appearance
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.ThemeBrightness
import com.arzabc.school.ui.theme.UiStyle

@Composable
fun AppDrawer(
    appearance: Appearance,
    onStyle: (UiStyle) -> Unit,
    onBrightness: (ThemeBrightness) -> Unit,
    onSeed: (ColorSeed) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    schoolDays: Int,
    onSchoolDays: (Int) -> Unit,
    onOpenBells: () -> Unit,
    onShareDay: () -> Unit,
    onShareWeek: () -> Unit,
    onImportSchedule: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    ModalDrawerSheet(
        drawerContainerColor = scheme.surfaceContainerLow,
        drawerContentColor = scheme.onSurface,
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DrawerLabel(stringResource(R.string.menu_appearance))
            NavigationDrawerItem(
                label = { Text(stringResource(UiStyle.Material.titleRes)) },
                selected = appearance.style == UiStyle.Material,
                onClick = { onStyle(UiStyle.Material) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            SeedSwatches(
                selected = appearance.seed,
                onSelect = onSeed,
                enabled = appearance.style == UiStyle.Material,
            )
            NavigationDrawerItem(
                label = { Text(stringResource(UiStyle.Glass.titleRes)) },
                selected = appearance.style == UiStyle.Glass,
                onClick = { onStyle(UiStyle.Glass) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Row(
                Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeBrightness.entries.forEach { item ->
                    FilterChip(
                        selected = appearance.brightness == item,
                        onClick = { onBrightness(item) },
                        label = { Text(stringResource(item.titleRes)) },
                    )
                }
            }

            DrawerLabel(stringResource(R.string.menu_language))
            AppLanguage.entries.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(stringResource(item.titleRes)) },
                    selected = language == item,
                    onClick = { onLanguage(item) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            DrawerLabel(stringResource(R.string.menu_week))
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.week_five_days)) },
                selected = schoolDays == 5,
                onClick = { onSchoolDays(5) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.week_six_days)) },
                selected = schoolDays == 6,
                onClick = { onSchoolDays(6) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            DrawerLabel(stringResource(R.string.menu_schedule))
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_bells)) },
                selected = false,
                onClick = onOpenBells,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_share_day)) },
                selected = false,
                onClick = onShareDay,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_share_week)) },
                selected = false,
                onClick = onShareWeek,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_import_schedule)) },
                selected = false,
                onClick = onImportSchedule,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun DrawerLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
    )
}
