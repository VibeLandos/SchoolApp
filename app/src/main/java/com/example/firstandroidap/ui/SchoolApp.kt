package com.example.firstandroidap.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.firstandroidap.ui.theme.CoverBurgundy
import com.example.firstandroidap.ui.theme.CoverDeep
import com.example.firstandroidap.ui.theme.CoverGold
import java.time.LocalDate

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
fun SchoolApp(viewModel: SchoolViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var editor by remember { mutableStateOf<Editor?>(null) }
    var lastDiaryTapAt by remember { mutableLongStateOf(0L) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CoverBurgundy,
        bottomBar = {
            NavigationBar(containerColor = CoverDeep) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CoverDeep,
                    selectedTextColor = CoverGold,
                    indicatorColor = CoverGold,
                    unselectedIconColor = CoverGold.copy(alpha = 0.7f),
                    unselectedTextColor = CoverGold.copy(alpha = 0.7f),
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
                    label = { Text("Дневник") },
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
                    label = { Text("Задания") },
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
