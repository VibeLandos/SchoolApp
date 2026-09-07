package com.arzabc.school.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arzabc.school.R
import com.arzabc.school.data.AppLanguage
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SchoolCatalog
import com.arzabc.school.ui.components.AppUpdateDialog
import com.arzabc.school.ui.components.EditHomeworkSheet
import com.arzabc.school.ui.components.EditLessonSheet
import com.arzabc.school.ui.components.ImportScheduleSheet
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.diary.DiaryScreen
import com.arzabc.school.ui.homework.HomeworkListScreen
import com.arzabc.school.ui.menu.AppDrawer
import com.arzabc.school.ui.menu.BellScheduleSheet
import com.arzabc.school.ui.now.NowScreen
import com.arzabc.school.ui.theme.Appearance
import com.arzabc.school.ui.theme.ColorSeed
import com.arzabc.school.ui.theme.GlassWallpaper
import com.arzabc.school.ui.theme.LocalGlassTokens
import com.arzabc.school.ui.theme.ThemeBrightness
import com.arzabc.school.ui.theme.UiStyle
import com.arzabc.school.ui.theme.glassSurface
import com.arzabc.school.update.GitHubUpdate
import com.arzabc.school.update.RemoteRelease
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    data object Update : Editor
}

@Composable
fun SchoolApp(
    viewModel: SchoolViewModel = viewModel(),
    appearance: Appearance,
    onStyle: (UiStyle) -> Unit,
    onBrightness: (ThemeBrightness) -> Unit,
    onSeed: (ColorSeed) -> Unit,
    language: AppLanguage,
    onLanguage: (AppLanguage) -> Unit,
) {
    val ui by viewModel.uiState.collectAsState()
    val glass = isGlassStyle()
    val context = LocalContext.current
    val shareDayTitle = stringResource(R.string.menu_share_day)
    val shareWeekTitle = stringResource(R.string.menu_share_week)
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    var editor by remember { mutableStateOf<Editor?>(null) }
    var launchUpdate by remember { mutableStateOf<RemoteRelease?>(null) }
    var lastDiaryTapAt by remember { mutableLongStateOf(0L) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val menuScope = rememberCoroutineScope()
    val openMenu: () -> Unit = { menuScope.launch { drawerState.open() } }
    val closeMenu: () -> Unit = { menuScope.launch { drawerState.close() } }
    val drawerShowing = drawerState.isOpen || drawerState.targetValue == DrawerValue.Open
    val openTasks = remember(ui.homework) { ui.homework.count { !it.isDone } }

    LaunchedEffect(Unit) {
        val remote = withContext(Dispatchers.IO) {
            runCatching { GitHubUpdate.fetchLatest() }.getOrNull()
        }
        if (remote != null && GitHubUpdate.isNewer(remote)) {
            launchUpdate = remote
        }
    }

    fun go(target: String) {
        navController.navigate(target) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun addLessonForSelected() {
        val date = ui.selectedDate
        val used = ui.lessons
            .filter { it.dayOfWeek == Dates.schoolDayOfWeek(date) }
            .map { it.period }
            .toSet()
        editor = Editor.LessonSlot(date, SchoolCatalog.nextFreePeriod(used), null)
    }

    BackHandler(enabled = drawerShowing) { closeMenu() }

    Box(Modifier.fillMaxSize()) {
        if (glass) {
            GlassWallpaper(dark = LocalGlassTokens.current.darkChrome)
        }
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerShowing,
            drawerContent = {
                AppDrawer(
                    appearance = appearance,
                    onStyle = onStyle,
                    onBrightness = onBrightness,
                    onSeed = onSeed,
                    language = language,
                    onLanguage = { next ->
                        onLanguage(next)
                        closeMenu()
                    },
                    schoolDays = ui.schoolDays,
                    onSchoolDays = viewModel::setSchoolDays,
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
                    onCheckUpdate = {
                        closeMenu()
                        editor = Editor.Update
                    },
                )
            },
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = if (glass) Color.Transparent else MaterialTheme.colorScheme.surface,
                floatingActionButton = {
                    if (!glass && route == Routes.Diary) {
                        FloatingActionButton(onClick = ::addLessonForSelected) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_lesson))
                        }
                    }
                },
                bottomBar = {
                    if (!glass) {
                        MaterialNavBar(
                            route = route,
                            openTasks = openTasks,
                            onDiary = {
                                val now = System.currentTimeMillis()
                                val onDiary = route == Routes.Diary
                                go(Routes.Diary)
                                if (onDiary && now - lastDiaryTapAt < 500L) viewModel.goToToday()
                                lastDiaryTapAt = now
                            },
                            onNow = { go(Routes.Now) },
                            onTasks = { go(Routes.Tasks) },
                        )
                    }
                },
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.Diary,
                    modifier = Modifier
                        .padding(padding)
                        .then(if (glass) Modifier.padding(bottom = 168.dp) else Modifier),
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
                            onToggleHomework = viewModel::toggleHomework,
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
                            schoolYear = ui.schoolYear,
                            onSaveSchoolYear = viewModel::setSchoolYear,
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
                                go(Routes.Diary)
                            },
                            onToggle = viewModel::toggleHomework,
                            onOpenMenu = openMenu,
                        )
                    }
                }
            }
            if (glass) {
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (route == Routes.Diary) {
                        GlassAddPill(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 18.dp, bottom = 12.dp),
                            onClick = ::addLessonForSelected,
                        )
                    }
                    GlassTabBar(
                        route = route,
                        openTasks = openTasks,
                        onDiary = {
                            val now = System.currentTimeMillis()
                            val onDiary = route == Routes.Diary
                            go(Routes.Diary)
                            if (onDiary && now - lastDiaryTapAt < 500L) viewModel.goToToday()
                            lastDiaryTapAt = now
                        },
                        onNow = { go(Routes.Now) },
                        onTasks = { go(Routes.Tasks) },
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
            Editor.Update -> AppUpdateDialog(onDismiss = { editor = null })
            null -> Unit
        }
        launchUpdate?.let { release ->
            if (editor !is Editor.Update) {
                AppUpdateDialog(
                    knownRelease = release,
                    onDismiss = { launchUpdate = null },
                )
            }
        }
    }
}

@Composable
private fun MaterialNavBar(
    route: String?,
    openTasks: Int,
    onDiary: () -> Unit,
    onNow: () -> Unit,
    onTasks: () -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = route == Routes.Diary,
            onClick = onDiary,
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_diary)) },
        )
        NavigationBarItem(
            selected = route == Routes.Now,
            onClick = onNow,
            icon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_now)) },
        )
        NavigationBarItem(
            selected = route == Routes.Tasks,
            onClick = onTasks,
            icon = {
                BadgedBox(badge = { if (openTasks > 0) Badge { Text(openTasks.toString()) } }) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null)
                }
            },
            label = { Text(stringResource(R.string.nav_tasks)) },
        )
    }
}

@Composable
private fun GlassTabBar(
    route: String?,
    openTasks: Int,
    onDiary: () -> Unit,
    onNow: () -> Unit,
    onTasks: () -> Unit,
) {
    val tokens = LocalGlassTokens.current
    Row(
        Modifier
            .glassSurface(tokens, 36.dp)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GlassTab(Icons.AutoMirrored.Outlined.MenuBook, stringResource(R.string.nav_diary), route == Routes.Diary, onDiary)
        GlassTab(Icons.Outlined.Schedule, stringResource(R.string.nav_now), route == Routes.Now, onNow)
        GlassTab(
            Icons.Outlined.CheckCircle,
            stringResource(R.string.nav_tasks),
            route == Routes.Tasks,
            onTasks,
            badge = openTasks,
        )
    }
}

@Composable
private fun GlassTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: Int = 0,
) {
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(28.dp)
    Row(
        Modifier
            .clip(shape)
            .then(
                if (selected) {
                    val selectedBg = if (tokens.darkChrome) {
                        Color.White.copy(alpha = 0.20f)
                    } else {
                        Color.White.copy(alpha = 0.92f)
                    }
                    Modifier.background(selectedBg, shape)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = if (selected) tokens.text else tokens.textSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = if (selected) tokens.text else tokens.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (badge > 0) {
            Box(
                Modifier
                    .padding(start = 6.dp)
                    .size(18.dp)
                    .background(tokens.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(badge.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GlassAddPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val tokens = LocalGlassTokens.current
    Row(
        modifier
            .glassSurface(tokens, 22.dp)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(24.dp)
                .background(tokens.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(
            text = stringResource(R.string.add_lesson),
            color = tokens.text,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
