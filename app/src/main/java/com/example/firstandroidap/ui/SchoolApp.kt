package com.example.firstandroidap.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firstandroidap.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.SchoolCatalog
import com.example.firstandroidap.ui.components.EditHomeworkSheet
import com.example.firstandroidap.ui.components.EditLessonSheet
import com.example.firstandroidap.ui.diary.DiaryScreen
import com.example.firstandroidap.ui.homework.HomeworkListScreen
import com.example.firstandroidap.ui.menu.AppDrawer
import com.example.firstandroidap.ui.theme.LocalDiaryPalette
import com.example.firstandroidap.ui.theme.ThemeMode
import java.time.LocalDate
import kotlinx.coroutines.launch

private object Routes {
    const val Diary = "diary"
    const val Tasks = "tasks"
}

private sealed interface Editor {
    data class LessonSlot(
        val date: LocalDate,
        val period: Int,
        val existing: Lesson?,
    ) : Editor

    data class HomeworkSlot(
        val date: LocalDate,
        val period: Int,
        val lesson: Lesson?,
        val existing: Homework?,
    ) : Editor
}

@Composable
fun SchoolApp(
    viewModel: SchoolViewModel = viewModel(),
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()
    val palette = LocalDiaryPalette.current
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var editor by remember { mutableStateOf<Editor?>(null) }
    var lastDiaryTapAt by remember { mutableLongStateOf(0L) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val menuScope = rememberCoroutineScope()
    val openMenu: () -> Unit = { menuScope.launch { drawerState.open() } }
    val closeMenu: () -> Unit = { menuScope.launch { drawerState.close() } }
    val drawerShowing = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open

    BackHandler(enabled = drawerShowing) { closeMenu() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerShowing,
        drawerContent = {
            AppDrawer(
                mode = themeMode,
                onMode = { mode ->
                    onThemeMode(mode)
                    closeMenu()
                },
            )
        },
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = palette.cover,
        bottomBar = {
            NavigationBar(containerColor = palette.coverDeep) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = palette.coverDeep,
                    selectedTextColor = palette.gold,
                    indicatorColor = palette.gold,
                    unselectedIconColor = palette.gold.copy(alpha = 0.7f),
                    unselectedTextColor = palette.gold.copy(alpha = 0.7f),
                )
                NavigationBarItem(
                    selected = route == Routes.Diary,
                    onClick = {
                        val now = System.currentTimeMillis()
                        val onDiary = route == Routes.Diary
                        navController.navigate(Routes.Diary) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        if (onDiary && now - lastDiaryTapAt < 500L) {
                            viewModel.goToToday()
                        }
                        lastDiaryTapAt = now
                    },
                    icon = { Icon(Icons.Outlined.MenuBook, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_diary)) },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = route == Routes.Tasks,
                    onClick = {
                        navController.navigate(Routes.Tasks) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_tasks)) },
                    colors = itemColors,
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Diary,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.Diary) {
                DiaryScreen(
                    selectedDate = ui.selectedDate,
                    weekDates = ui.weekDates,
                    lessons = ui.lessons,
                    homework = ui.homework,
                    photos = ui.photos,
                    onSelectDate = viewModel::selectDate,
                    onShiftWeek = viewModel::shiftWeek,
                    onSubjectClick = { date, period, lesson ->
                        editor = Editor.LessonSlot(date, period, lesson)
                    },
                    onHomeworkClick = { date, period, lesson, homework ->
                        editor = Editor.HomeworkSlot(date, period, lesson, homework)
                    },
                    onOpenMenu = openMenu,
                )
            }
            composable(Routes.Tasks) {
                HomeworkListScreen(
                    lessons = ui.lessons,
                    homework = ui.homework,
                    photos = ui.photos,
                    onOpenDate = { date ->
                        viewModel.selectDate(
                            if (date.dayOfWeek.value == 7) date.plusDays(1) else date,
                        )
                        navController.navigate(Routes.Diary) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onToggle = viewModel::toggleHomework,
                    onOpenMenu = openMenu,
                )
            }
        }
    }

    when (val current = editor) {
        is Editor.LessonSlot -> EditLessonSheet(
            date = current.date,
            period = current.period,
            existing = current.existing,
            onDismiss = { editor = null },
            onSave = viewModel::saveLesson,
            onDelete = viewModel::deleteLesson,
        )
        is Editor.HomeworkSlot -> EditHomeworkSheet(
            date = current.date,
            period = current.period,
            lesson = current.lesson,
            existing = current.existing,
            existingPhotos = ui.photos.filter { it.homeworkId == current.existing?.id },
            photoFile = viewModel::photoFile,
            onDismiss = { editor = null },
            onSave = { homework, subjectIfNeeded, keepFileNames, newUris ->
                viewModel.saveHomework(homework, keepFileNames, newUris)
                if (current.lesson == null && subjectIfNeeded.isNotBlank()) {
                    val bells = SchoolCatalog.bells(current.period)
                    viewModel.saveLesson(
                        Lesson(
                            dayOfWeek = Dates.schoolDayOfWeek(current.date),
                            period = current.period,
                            subject = subjectIfNeeded,
                            startTime = bells.first,
                            endTime = bells.second,
                        ),
                    )
                }
            },
            onDelete = viewModel::deleteHomework,
        )
        null -> Unit
    }
    }
}
