package com.arzabc.school.ui.homework

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.subjectFor
import com.arzabc.school.ui.components.DiaryCard
import com.arzabc.school.ui.components.PeriodBadge
import com.arzabc.school.ui.components.ScreenHeader
import com.arzabc.school.ui.components.WeekDayChips
import com.arzabc.school.ui.components.isGlassStyle
import com.arzabc.school.ui.theme.LocalGlassTokens
import java.time.LocalDate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeworkListScreen(
    selectedDate: LocalDate,
    weekDates: List<LocalDate>,
    schoolDays: Int,
    lessons: List<Lesson>,
    homework: List<Homework>,
    photos: List<HomeworkPhoto>,
    onSelectDate: (LocalDate) -> Unit,
    onShiftDay: (Int) -> Unit,
    onOpenHomework: (Homework) -> Unit,
    onToggle: (Homework) -> Unit,
    onAddHomework: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val homeworkByDay = remember(homework) { homework.groupBy { it.epochDay } }
    val photosByHomework = remember(photos) { photos.groupBy { it.homeworkId } }
    val startPage = Dates.dayIndex(selectedDate, schoolDays)
    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { weekDates.size.coerceAtLeast(1) },
    )
    val pagerScope = rememberCoroutineScope()
    val weekDatesState = rememberUpdatedState(weekDates)
    val onSelectDateState = rememberUpdatedState(onSelectDate)

    LaunchedEffect(selectedDate) {
        val target = Dates.dayIndex(selectedDate, schoolDays)
        if (pagerState.currentPage == target || pagerState.isScrollInProgress) return@LaunchedEffect
        if (kotlin.math.abs(pagerState.currentPage - target) == 1) {
            pagerState.animateScrollToPage(target)
        } else {
            pagerState.scrollToPage(target)
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val date = weekDatesState.value.getOrNull(page) ?: return@collect
                onSelectDateState.value(date)
            }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = Dates.weekdayName(selectedDate),
            subtitle = Dates.formatFull(selectedDate),
            onOpenMenu = onOpenMenu,
            actions = {
                IconButton(onClick = { onShiftDay(-1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.cd_prev_day),
                    )
                }
                IconButton(onClick = { onShiftDay(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.cd_next_day),
                    )
                }
            },
        )
        WeekDayChips(
            selectedDate = selectedDate,
            weekDates = weekDates,
            today = today,
            showDate = true,
            onSelectDate = { date ->
                onSelectDate(date)
                val target = Dates.dayIndex(date, schoolDays)
                pagerScope.launch { pagerState.animateScrollToPage(target) }
            },
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            beyondBoundsPageCount = 0,
        ) { page ->
            val date = weekDates.getOrNull(page) ?: selectedDate
            HomeworkDayPage(
                items = homeworkByDay[date.toEpochDay()].orEmpty().sortedBy { it.period },
                lessons = lessons,
                photosByHomework = photosByHomework,
                onOpenHomework = onOpenHomework,
                onToggle = onToggle,
                onAddHomework = onAddHomework,
            )
        }
    }
}

@Composable
private fun HomeworkDayPage(
    items: List<Homework>,
    lessons: List<Lesson>,
    photosByHomework: Map<Long, List<HomeworkPhoto>>,
    onOpenHomework: (Homework) -> Unit,
    onToggle: (Homework) -> Unit,
    onAddHomework: () -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val muted = if (glass) tokens.textSecondary else scheme.onSurfaceVariant
    val bottomGap = if (glass) 28.dp else 88.dp
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = bottomGap),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (items.isEmpty()) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.tasks_empty_day),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = if (glass) tokens.text else scheme.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.tasks_empty_body),
                        textAlign = TextAlign.Center,
                        color = muted,
                        modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp),
                    )
                    FilledTonalButton(
                        onClick = onAddHomework,
                        modifier = Modifier.padding(top = 20.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.add_homework))
                    }
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                HomeworkCard(
                    item = item,
                    subject = subjectFor(item, lessons),
                    photoCount = photosByHomework[item.id]?.size ?: 0,
                    onOpen = { onOpenHomework(item) },
                    onToggle = { onToggle(item) },
                )
            }
        }
    }
}

@Composable
private fun HomeworkCard(
    item: Homework,
    subject: String?,
    photoCount: Int,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val glass = isGlassStyle()
    val tokens = LocalGlassTokens.current
    val scheme = MaterialTheme.colorScheme
    val muted = if (glass) tokens.textSecondary else scheme.onSurfaceVariant
    val title = when {
        item.subject.isNotBlank() -> item.subject
        subject != null -> subject
        else -> stringResource(R.string.lesson_n, item.period)
    }
    DiaryCard(onClick = onOpen) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.subject.isBlank()) {
                PeriodBadge(item.period)
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = if (item.subject.isBlank()) 10.dp else 0.dp, end = 4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (glass) tokens.text else LocalContentColor.current,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = muted,
                        textDecoration = if (item.isDone) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (photoCount > 0) {
                    Text(
                        text = pluralStringResource(R.plurals.photo_count, photoCount, photoCount),
                        color = muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            Checkbox(
                checked = item.isDone,
                onCheckedChange = { onToggle() },
            )
        }
    }
}
