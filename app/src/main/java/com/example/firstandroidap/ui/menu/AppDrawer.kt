package com.example.firstandroidap.ui.menu

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
import com.example.firstandroidap.R
import com.example.firstandroidap.data.AppLanguage
import com.example.firstandroidap.ui.theme.LocalDiaryPalette
import com.example.firstandroidap.ui.theme.ThemeMode

@Composable
fun AppDrawer(
    mode: ThemeMode,
    onMode: (ThemeMode) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
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
        }
    }
}
