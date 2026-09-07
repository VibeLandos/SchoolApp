package com.arzabc.school.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arzabc.school.R
import com.arzabc.school.data.Dates
import com.arzabc.school.data.SchoolYear
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private enum class YearField { Start, End }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolYearSheet(
    year: SchoolYear,
    onDismiss: () -> Unit,
    onSave: (SchoolYear) -> Unit,
) {
    var start by remember(year) { mutableStateOf(year.start) }
    var end by remember(year) { mutableStateOf(year.end) }
    var picking by remember { mutableStateOf<YearField?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(text = stringResource(R.string.year_sheet_title), fontSize = 22.sp)
            Spacer(Modifier.height(16.dp))
            Text(text = stringResource(R.string.year_start), fontSize = 13.sp)
            OutlinedButton(
                onClick = { picking = YearField.Start },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
            ) {
                Text(Dates.formatFull(start))
            }
            Text(text = stringResource(R.string.year_end), fontSize = 13.sp)
            OutlinedButton(
                onClick = { picking = YearField.End },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp),
            ) {
                Text(Dates.formatFull(end))
            }
            Button(
                onClick = {
                    onSave(SchoolYear(start, end).normalized())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    val field = picking
    if (field != null) {
        key(field) {
            YearDatePicker(
                initial = if (field == YearField.Start) start else end,
                onDismiss = { picking = null },
                onPick = { date ->
                    if (field == YearField.Start) start = date else end = date
                    picking = null
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearDatePicker(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPick: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    onPick(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = state)
    }
}
