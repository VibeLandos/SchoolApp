package com.arzabc.school.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arzabc.school.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SchoolCatalog
import com.arzabc.school.ui.components.EditHomeworkSheet
import com.arzabc.school.ui.components.EditLessonSheet
import com.arzabc.school.ui.components.ImportScheduleSheet
import com.arzabc.school.ui.diary.DiaryScreen
import com.arzabc.school.ui.homework.HomeworkListScreen
import com.arzabc.school.ui.menu.AppDrawer
import com.arzabc.school.ui.menu.BellScheduleSheet
import com.arzabc.school.ui.now.NowScreen
import com.arzabc.school.ui.theme.LocalDiaryPalette
import com.arzabc.school.ui.theme.ThemeMode
import java.time.LocalDate
import kotlinx.coroutines.launch

private object Routes {
    const val Diary = "diary"
    const val Now = "now"
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

    data object Bells : Editor
    data object Import : Editor
}

@Composable
fun SchoolApp(
    viewModel: SchoolViewModel = viewModel(),
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()
    val palette = LocalDiaryPalette.current
    val context = LocalContext.current
    val shareDayTitle = stringResource(R.string.menu_share_day)
    val shareWeekTitle = stringResource(R.string.menu_share_week)
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
                language = language,
                onLanguage = { next ->
                    onLanguage(next)
                    closeMenu()
                },
                schoolDays = ui.schoolDays,
                onSchoolDays = { days ->
                    viewModel.setSchoolDays(days)
                    closeMenu()
                },
                onOpenBells = {
                    closeMenu()
                    editor = Editor.Bells
                },
                onShareDay = {
                    closeMenu()
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, viewModel.shareDayText())
                    }
                    context.startActivity(Intent.createChooser(send, shareDayTitle))
                },
                onShareWeek = {
                    closeMenu()
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, viewModel.shareWeekText())
                    }
                    context.startActivity(Intent.createChooser(send, shareWeekTitle))
                },
                onImportSchedule = {
                    closeMenu()
                    editor = Editor.Import
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
                    selected = route == Routes.Now,
                    onClick = {
                        navController.navigate(Routes.Now) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_now)) },
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
                    weekBells = ui.weekBells,
                    schoolDays = ui.schoolDays,
                    onSelectDate = viewModel::selectDate,
                    onShiftWeek = viewModel::shiftWeek,
                    onSubjectClick = { date, period, lesson ->
                        editor = Editor.LessonSlot(date, period, lesson)
                    },
                    onHomeworkClick = { date, period, lesson, homework ->
                        editor = Editor.HomeworkSlot(date, period, lesson, homework)
                    },
                    onAddLesson = { date ->
                        val used = ui.lessons
                            .filter { it.dayOfWeek == Dates.schoolDayOfWeek(date) }
                            .map { it.period }
                            .toSet()
                        editor = Editor.LessonSlot(
                            date,
                            SchoolCatalog.nextFreePeriod(used),
                            null,
                        )
                    },
                    onOpenMenu = openMenu,
                )
            }
            composable(Routes.Now) {
                NowScreen(
                    lessons = ui.lessons,
                    weekBells = ui.weekBells,
                    schoolDays = ui.schoolDays,
                    onOpenMenu = openMenu,
                )
            }
            composable(Routes.Tasks) {
                HomeworkListScreen(
                    lessons = ui.lessons,
                    homework = ui.homework,
                    photos = ui.photos,
                    onOpenDate = { date ->
                        viewModel.selectDate(date)
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
        is Editor.LessonSlot -> {
            val day = Dates.schoolDayOfWeek(current.date)
            EditLessonSheet(
                date = current.date,
                period = current.period,
                existing = current.existing,
                usedPeriods = ui.lessons.filter { it.dayOfWeek == day }.map { it.period }.toSet(),
                bells = ui.weekBells.forDay(day),
                onDismiss = { editor = null },
                onSave = viewModel::saveLesson,
                onDelete = viewModel::deleteLesson,
            )
        }
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
                    val slot = ui.weekBells.forDay(Dates.schoolDayOfWeek(current.date)).of(current.period)
                    viewModel.saveLesson(
                        Lesson(
                            dayOfWeek = Dates.schoolDayOfWeek(current.date),
                            period = current.period,
                            subject = subjectIfNeeded,
                            startTime = slot.start,
                            endTime = slot.end,
                        ),
                    )
                }
            },
            onDelete = viewModel::deleteHomework,
        )
        Editor.Bells -> BellScheduleSheet(
            weekBells = ui.weekBells,
            schoolDays = ui.schoolDays,
            onChange = viewModel::setWeekBells,
            onDismiss = { editor = null },
        )
        Editor.Import -> ImportScheduleSheet(
            onDismiss = { editor = null },
            onImport = viewModel::importScheduleText,
        )
        null -> Unit
    }
    }
}
