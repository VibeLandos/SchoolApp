package com.arzabc.school.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.ui.theme.LocalDiaryPalette
import com.arzabc.school.ui.theme.ThemeMode

@Composable
fun AppDrawer(
    mode: ThemeMode,
    onMode: (ThemeMode) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
    schoolDays: Int,
    onSchoolDays: (Int) -> Unit,
    onOpenBells: () -> Unit,
    onShareDay: () -> Unit,
    onShareWeek: () -> Unit,
    onImportSchedule: () -> Unit,
) {
    val palette = LocalDiaryPalette.current
    ModalDrawerSheet(
        drawerContainerColor = palette.coverDeep,
        drawerContentColor = palette.gold,
    ) {
        val itemColors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = palette.cover,
            selectedTextColor = palette.gold,
            unselectedTextColor = palette.gold.copy(alpha = 0.8f),
            unselectedContainerColor = palette.coverDeep,
        )
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(R.string.menu_appearance),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            )
            ThemeMode.entries.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(stringResource(item.titleRes)) },
                    selected = mode == item,
                    onClick = { onMode(item) },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Text(
                text = stringResource(R.string.menu_language),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            )
            AppLanguage.entries.forEach { item ->
                NavigationDrawerItem(
                    label = { Text(stringResource(item.titleRes)) },
                    selected = language == item,
                    onClick = { onLanguage(item) },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
            Text(
                text = stringResource(R.string.menu_week),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.week_five_days)) },
                selected = schoolDays == 5,
                onClick = { onSchoolDays(5) },
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.week_six_days)) },
                selected = schoolDays == 6,
                onClick = { onSchoolDays(6) },
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            Text(
                text = stringResource(R.string.menu_schedule),
                color = palette.gold,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_bells)) },
                selected = false,
                onClick = onOpenBells,
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_share_day)) },
                selected = false,
                onClick = onShareDay,
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_share_week)) },
                selected = false,
                onClick = onShareWeek,
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(R.string.menu_import_schedule)) },
                selected = false,
                onClick = onImportSchedule,
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}
