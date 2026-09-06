package com.arzabc.school.ui.menu

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.BellRecipe
import com.arzabc.school.data.BellSchedule
import com.arzabc.school.data.Dates
import com.arzabc.school.data.SchoolCatalog
import com.arzabc.school.data.SpecialBreak
import com.arzabc.school.data.WeekBells
import com.arzabc.school.data.parseHm

private sealed interface BellsPane {
    data object Offer : BellsPane
    data class Wizard(val step: Int, val targetDay: Int?) : BellsPane
    data class Editor(val day: Int) : BellsPane
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BellScheduleSheet(
    weekBells: WeekBells,
    schoolDays: Int,
    onChange: (WeekBells) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Dates.todaySchoolDate(schoolDays).dayOfWeek.value.coerceIn(1, schoolDays)
    var pane by remember {
        mutableStateOf(
            if (weekBells.setupDone) BellsPane.Editor(today) else BellsPane.Offer,
        )
    }
    var recipe by remember { mutableStateOf(BellRecipe.standard()) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            when (val current = pane) {
                BellsPane.Offer -> OfferPane(
                    onStart = {
                        recipe = BellRecipe.standard()
                        pane = BellsPane.Wizard(step = 1, targetDay = null)
                    },
                    onLater = {
                        onChange(weekBells.markedSetup())
                        pane = BellsPane.Editor(today)
                    },
                )
                is BellsPane.Wizard -> WizardPane(
                    step = current.step,
                    recipe = recipe,
                    applyToWeek = current.targetDay == null,
                    onRecipe = { recipe = it },
                    onBack = {
                        if (current.step == 1) {
                            pane = if (weekBells.setupDone) {
                                BellsPane.Editor(current.targetDay ?: today)
                            } else {
                                BellsPane.Offer
                            }
                        } else {
                            pane = current.copy(step = current.step - 1)
                        }
                    },
                    onNext = { pane = current.copy(step = current.step + 1) },
                    onDone = {
                        val built = recipe.build()
                        onChange(
                            if (current.targetDay == null) {
                                weekBells.withWeek(built)
                            } else {
                                weekBells.withDay(current.targetDay, built)
                            },
                        )
                        pane = BellsPane.Editor(current.targetDay ?: today)
                    },
                    pickStart = {
                        pickTime(context, recipe.firstStart) { time ->
                            recipe = recipe.copy(firstStart = time)
                        }
                    },
                )
                is BellsPane.Editor -> EditorPane(
                    weekBells = weekBells,
                    schoolDays = schoolDays,
                    day = current.day.coerceIn(1, schoolDays),
                    onDay = { pane = BellsPane.Editor(it) },
                    onChange = onChange,
                    onQuickSetup = { forDay ->
                        recipe = BellRecipe.standard()
                        pane = BellsPane.Wizard(step = 1, targetDay = forDay)
                    },
                    pickTime = { currentTime, onPicked ->
                        pickTime(context, currentTime, onPicked)
                    },
                )
            }
        }
    }
}

@Composable
private fun OfferPane(
    onStart: () -> Unit,
    onLater: () -> Unit,
) {
    Text(stringResource(R.string.bells_title), fontSize = 22.sp)
    Text(
        text = stringResource(R.string.bells_quick_title),
        fontSize = 20.sp,
        modifier = Modifier.padding(top = 16.dp),
    )
    Text(
        text = stringResource(R.string.bells_quick_body),
        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
    )
    Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.bells_quick_start))
    }
    TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.bells_quick_later))
    }
}

@Composable
private fun WizardPane(
    step: Int,
    recipe: BellRecipe,
    applyToWeek: Boolean,
    onRecipe: (BellRecipe) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onDone: () -> Unit,
    pickStart: () -> Unit,
) {
    Text(stringResource(R.string.bells_title), fontSize = 22.sp)
    Text(
        text = stringResource(R.string.bells_quick_step, step, 3),
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
    )
    when (step) {
        1 -> {
            Text(stringResource(R.string.bells_ask_lesson), fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            ChipRow(listOf(35, 40, 45, 50), recipe.lessonMinutes, minutes = true) {
                onRecipe(recipe.copy(lessonMinutes = it))
            }
            Text(
                text = stringResource(R.string.bells_ask_first),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            Text(
                text = recipe.firstStart,
                fontSize = 22.sp,
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable(onClick = pickStart)
                    .padding(vertical = 8.dp),
            )
        }
        2 -> {
            Text(stringResource(R.string.bells_ask_break), fontSize = 18.sp)
            Text(
                text = stringResource(R.string.bells_ask_break_hint),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            ChipRow(listOf(5, 10, 15), recipe.breakMinutes, minutes = true) {
                onRecipe(recipe.copy(breakMinutes = it))
            }
        }
        else -> {
            Text(stringResource(R.string.bells_ask_other), fontSize = 18.sp)
            Text(
                text = stringResource(R.string.bells_ask_other_hint),
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            recipe.specialBreaks.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.bells_after_period, item.afterPeriod, item.minutes),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = {
                            onRecipe(recipe.copy(specialBreaks = recipe.specialBreaks - item))
                        },
                    ) {
                        Text(stringResource(R.string.bells_remove_break))
                    }
                }
            }
            if (recipe.specialBreaks.size < 4) {
                TextButton(
                    onClick = {
                        val used = recipe.specialBreaks.map { it.afterPeriod }.toSet()
                        val after = (1 until SchoolCatalog.PERIODS).firstOrNull { it !in used } ?: 3
                        onRecipe(
                            recipe.copy(
                                specialBreaks = recipe.specialBreaks + SpecialBreak(after, 20),
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.bells_add_break))
                }
            }
            val editing = recipe.specialBreaks.lastOrNull()
            if (editing != null) {
                Text(stringResource(R.string.bells_after_which), modifier = Modifier.padding(top = 8.dp))
                ChipRow((1 until SchoolCatalog.PERIODS).toList(), editing.afterPeriod, minutes = false) { period ->
                    val rest = recipe.specialBreaks.dropLast(1).filter { it.afterPeriod != period }
                    onRecipe(recipe.copy(specialBreaks = rest + editing.copy(afterPeriod = period)))
                }
                Text(stringResource(R.string.bells_other_length), modifier = Modifier.padding(top = 8.dp))
                ChipRow(listOf(15, 20, 25, 30), editing.minutes, minutes = true) { minutes ->
                    onRecipe(
                        recipe.copy(
                            specialBreaks = recipe.specialBreaks.dropLast(1) + editing.copy(minutes = minutes),
                        ),
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.bells_back)) }
        if (step < 3) {
            Button(onClick = onNext) { Text(stringResource(R.string.bells_next)) }
        } else {
            Button(onClick = onDone) {
                Text(
                    stringResource(
                        if (applyToWeek) R.string.bells_apply_week else R.string.bells_apply_day,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EditorPane(
    weekBells: WeekBells,
    schoolDays: Int,
    day: Int,
    onDay: (Int) -> Unit,
    onChange: (WeekBells) -> Unit,
    onQuickSetup: (Int?) -> Unit,
    pickTime: (String, (String) -> Unit) -> Unit,
) {
    val schedule = weekBells.forDay(day)
    val custom = weekBells.isCustom(day)
    val sampleWeek = remember(schoolDays) { Dates.weekDates(Dates.todaySchoolDate(schoolDays), schoolDays) }
    val selectedDate = sampleWeek.first { it.dayOfWeek.value == day }
    Text(stringResource(R.string.bells_title), fontSize = 22.sp)
    Text(
        text = stringResource(
            R.string.bells_selected_day,
            Dates.weekdayName(selectedDate),
            stringResource(if (custom) R.string.bells_day_custom else R.string.bells_day_week),
        ),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        sampleWeek.forEach { date ->
            val value = date.dayOfWeek.value
            val selected = value == day
            Column(
                modifier = Modifier
                    .clickable { onDay(value) }
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = Dates.weekdayShort(date),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    },
                    modifier = if (selected) {
                        Modifier
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    } else {
                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    },
                )
                Text(
                    text = if (weekBells.isCustom(value)) "·" else " ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    repeat(SchoolCatalog.PERIODS) { index ->
        val period = index + 1
        val slot = schedule.of(period)
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = period.toString(),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(36.dp),
            )
            Text(
                text = slot.start,
                fontSize = 18.sp,
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable {
                        pickTime(slot.start) { time ->
                            val base = if (custom) schedule else weekBells.week
                            onChange(weekBells.withDay(day, base.replacing(period, start = time)))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            Text("–", fontSize = 18.sp)
            Text(
                text = slot.end,
                fontSize = 18.sp,
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable {
                        pickTime(slot.end) { time ->
                            val base = if (custom) schedule else weekBells.week
                            onChange(weekBells.withDay(day, base.replacing(period, end = time)))
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
    OutlinedButton(
        onClick = { onQuickSetup(null) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.bells_quick_again))
    }
    TextButton(
        onClick = { onQuickSetup(day) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.bells_quick_this_day))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipRow(
    values: List<Int>,
    selected: Int,
    minutes: Boolean,
    onSelect: (Int) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        values.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelect(value) },
                label = {
                    Text(
                        if (minutes) {
                            stringResource(R.string.bells_minutes, value)
                        } else {
                            value.toString()
                        },
                    )
                },
            )
        }
    }
}

private fun pickTime(context: Context, current: String, onPicked: (String) -> Unit) {
    val parsed = parseHm(current)
    val hour = parsed?.hour ?: 8
    val minute = parsed?.minute ?: 0
    TimePickerDialog(
        context,
        { _, nextHour, nextMinute ->
            onPicked("%02d:%02d".format(nextHour, nextMinute))
        },
        hour,
        minute,
        true,
    ).show()
}
