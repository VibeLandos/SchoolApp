package com.arzabc.school.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arzabc.school.SchoolApplication
import com.arzabc.school.data.Dates
import com.arzabc.school.data.Homework
import com.arzabc.school.data.HomeworkPhoto
import com.arzabc.school.data.Lesson
import com.arzabc.school.data.SchedulePayload
import com.arzabc.school.data.ScheduleText
import com.arzabc.school.data.SchoolYear
import com.arzabc.school.data.WeekBells
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiaryUiState(
    val selectedDate: LocalDate = Dates.todaySchoolDate(),
    val taskDate: LocalDate = Dates.todaySchoolDate(),
    val weekDates: List<LocalDate> = Dates.weekDates(Dates.todaySchoolDate()),
    val taskWeekDates: List<LocalDate> = Dates.weekDates(Dates.todaySchoolDate()),
    val lessons: List<Lesson> = emptyList(),
    val homework: List<Homework> = emptyList(),
    val photos: List<HomeworkPhoto> = emptyList(),
    val weekBells: WeekBells = WeekBells(),
    val schoolDays: Int = 6,
    val schoolYear: SchoolYear = SchoolYear.academicFor(),
)

class SchoolViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SchoolApplication).repository
    private val settings = (application as SchoolApplication).themeSettings
    private val selectedDate = MutableStateFlow(Dates.todaySchoolDate(settings.schoolDays.value))
    private val taskDate = MutableStateFlow(Dates.todaySchoolDate(settings.schoolDays.value))

    val uiState: StateFlow<DiaryUiState> = combine(
        combine(selectedDate, taskDate) { selected, task -> selected to task },
        repository.lessons,
        repository.homework,
        repository.photos,
        combine(settings.weekBells, settings.schoolDays, settings.schoolYear) { bells, days, year ->
            Triple(bells, days, year)
        },
    ) { dates, lessons, homework, photos, settingsSlice ->
        val (selected, task) = dates
        val (weekBells, schoolDays, schoolYear) = settingsSlice
        val clamped = Dates.clampToSchoolWeek(selected, schoolDays)
        val clampedTask = Dates.clampToSchoolWeek(task, schoolDays)
        DiaryUiState(
            selectedDate = clamped,
            taskDate = clampedTask,
            weekDates = Dates.weekDates(clamped, schoolDays),
            taskWeekDates = Dates.weekDates(clampedTask, schoolDays),
            lessons = lessons,
            homework = homework,
            photos = photos,
            weekBells = weekBells,
            schoolDays = schoolDays,
            schoolYear = schoolYear,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        DiaryUiState(),
    )

    fun photoFile(fileName: String): File = repository.photoFile(fileName)

    fun goToToday() {
        selectedDate.value = Dates.todaySchoolDate(settings.schoolDays.value)
    }

    fun selectDate(date: LocalDate) {
        val next = Dates.clampToSchoolWeek(date, settings.schoolDays.value)
        if (selectedDate.value != next) selectedDate.value = next
    }

    fun selectTaskDate(date: LocalDate) {
        val next = Dates.clampToSchoolWeek(date, settings.schoolDays.value)
        if (taskDate.value != next) taskDate.value = next
    }

    fun shiftTaskWeek(weeks: Long) {
        val days = settings.schoolDays.value
        taskDate.value = Dates.clampToSchoolWeek(taskDate.value.plusWeeks(weeks), days)
    }

    fun saveLesson(lesson: Lesson) {
        viewModelScope.launch { repository.saveLesson(lesson) }
    }

    fun deleteLesson(lesson: Lesson) {
        viewModelScope.launch { repository.deleteLesson(lesson) }
    }

    fun saveHomework(
        homework: Homework,
        keepFileNames: List<String>,
        newUris: List<Uri>,
    ) {
        viewModelScope.launch { repository.saveHomework(homework, keepFileNames, newUris) }
    }

    fun deleteHomework(homework: Homework) {
        viewModelScope.launch { repository.deleteHomework(homework) }
    }

    fun toggleHomework(homework: Homework) {
        viewModelScope.launch { repository.toggleHomework(homework) }
    }

    fun setWeekBells(value: WeekBells) {
        settings.setWeekBells(value)
    }

    fun setSchoolDays(days: Int) {
        val value = days.coerceIn(5, 6)
        settings.setSchoolDays(value)
        selectedDate.value = Dates.clampToSchoolWeek(selectedDate.value, value)
        taskDate.value = Dates.clampToSchoolWeek(taskDate.value, value)
    }

    fun setSchoolYear(year: SchoolYear) {
        settings.setSchoolYear(year)
    }

    fun shareDayText(): String {
        val state = uiState.value
        val date = state.selectedDate
        val day = Dates.schoolDayOfWeek(date)
        return ScheduleText.encodeDay(
            dayOfWeek = day,
            lessons = state.lessons,
            bells = state.weekBells.forDay(day),
            weekdayTitle = Dates.weekdayName(date),
        )
    }

    fun shareWeekText(): String {
        val state = uiState.value
        val monday = Dates.weekMonday(state.selectedDate)
        return ScheduleText.encodeWeek(
            schoolDays = state.schoolDays,
            lessons = state.lessons,
            weekBells = state.weekBells,
            dayTitle = { day -> Dates.weekdayName(monday.plusDays((day - 1).toLong())) },
        )
    }

    fun importScheduleText(raw: String): Boolean {
        val parsed = ScheduleText.decode(raw) ?: return false
        val state = uiState.value
        when (parsed) {
            is SchedulePayload.Day -> {
                val day = Dates.schoolDayOfWeek(state.selectedDate)
                val bells = parsed.value.bells
                val lessons = parsed.value.lessons.map { lesson ->
                    val slot = bells.of(lesson.period)
                    lesson.copy(
                        id = 0,
                        dayOfWeek = day,
                        startTime = slot.start,
                        endTime = slot.end,
                    )
                }
                settings.setWeekBells(state.weekBells.withDay(day, bells), sync = true)
                viewModelScope.launch(NonCancellable + Dispatchers.IO) {
                    repository.replaceDayLessons(day, lessons)
                }
            }
            is SchedulePayload.Week -> {
                val payload = parsed.value
                settings.setSchoolDays(payload.schoolDays, sync = true)
                settings.setWeekBells(payload.weekBells, sync = true)
                selectedDate.value = Dates.clampToSchoolWeek(state.selectedDate, payload.schoolDays)
                val lessons = payload.lessons.map { lesson ->
                    val slot = payload.weekBells.forDay(lesson.dayOfWeek).of(lesson.period)
                    lesson.copy(id = 0, startTime = slot.start, endTime = slot.end)
                }
                viewModelScope.launch(NonCancellable + Dispatchers.IO) {
                    repository.replaceWeekLessons(lessons)
                }
            }
        }
        return true
    }
}
