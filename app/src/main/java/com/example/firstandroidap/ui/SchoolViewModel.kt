package com.example.firstandroidap.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstandroidap.SchoolApplication
import com.example.firstandroidap.data.Dates
import com.example.firstandroidap.data.Homework
import com.example.firstandroidap.data.HomeworkPhoto
import com.example.firstandroidap.data.Lesson
import com.example.firstandroidap.data.SchedulePayload
import com.example.firstandroidap.data.ScheduleText
import com.example.firstandroidap.data.WeekBells
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiaryUiState(
    val selectedDate: LocalDate = Dates.todaySchoolDate(),
    val weekDates: List<LocalDate> = Dates.weekDates(Dates.todaySchoolDate()),
    val lessons: List<Lesson> = emptyList(),
    val homework: List<Homework> = emptyList(),
    val photos: List<HomeworkPhoto> = emptyList(),
    val weekBells: WeekBells = WeekBells(),
    val schoolDays: Int = 6,
)

class SchoolViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as SchoolApplication).repository
    private val settings = (application as SchoolApplication).themeSettings
    private val selectedDate = MutableStateFlow(Dates.todaySchoolDate(settings.schoolDays.value))

    val uiState: StateFlow<DiaryUiState> = combine(
        selectedDate,
        repository.lessons,
        repository.homework,
        repository.photos,
        combine(settings.weekBells, settings.schoolDays) { bells, days -> bells to days },
    ) { date, lessons, homework, photos, bellsAndDays ->
        val (weekBells, schoolDays) = bellsAndDays
        val clamped = Dates.clampToSchoolWeek(date, schoolDays)
        DiaryUiState(
            selectedDate = clamped,
            weekDates = Dates.weekDates(clamped, schoolDays),
            lessons = lessons,
            homework = homework,
            photos = photos,
            weekBells = weekBells,
            schoolDays = schoolDays,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
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

    fun shiftWeek(weeks: Long) {
        selectedDate.value = selectedDate.value.plusWeeks(weeks)
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
                settings.setWeekBells(state.weekBells.withDay(day, bells))
                viewModelScope.launch { repository.replaceDayLessons(day, lessons) }
            }
            is SchedulePayload.Week -> {
                val payload = parsed.value
                settings.setSchoolDays(payload.schoolDays)
                settings.setWeekBells(payload.weekBells)
                selectedDate.value = Dates.clampToSchoolWeek(state.selectedDate, payload.schoolDays)
                val lessons = payload.lessons.map { lesson ->
                    val slot = payload.weekBells.forDay(lesson.dayOfWeek).of(lesson.period)
                    lesson.copy(id = 0, startTime = slot.start, endTime = slot.end)
                }
                viewModelScope.launch { repository.replaceWeekLessons(lessons) }
            }
        }
        return true
    }
}
