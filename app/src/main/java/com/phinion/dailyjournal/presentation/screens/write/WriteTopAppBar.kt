package com.phinion.dailyjournal.presentation.screens.write

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.phinion.dailyjournal.model.Diary
import com.phinion.dailyjournal.presentation.components.DisplayAlertDialog
import com.phinion.dailyjournal.util.toInstant
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteTopAppBar(
    selectedDiary: Diary?,
    onBackPressed: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    moodName: () -> String,
    onUpdatedDateTime: (ZonedDateTime) -> Unit
) {

    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()

    var currentDate by remember {
        mutableStateOf(LocalDate.now())
    }
    var currentTime by remember {
        mutableStateOf(LocalTime.now())
    }

    val formattedDate = remember(key1 = currentDate) {
        DateTimeFormatter
            .ofPattern("dd MMM yyyy")
            .format(currentDate).uppercase()
    }

    var dateTimeUpdated by remember {
        mutableStateOf(false)
    }

    val formattedTime = remember(key1 = currentTime) {
        DateTimeFormatter
            .ofPattern("hh:mm a")
            .format(currentTime).uppercase()
    }

    val selectedDiaryDateTime = remember(selectedDiary) {
        if (selectedDiary != null) {
            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(Date.from(selectedDiary.date.toInstant())).uppercase()
        } else {
            "Unknown"
        }

    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back button")
            }
        },
        title = {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = moodName(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (selectedDiary != null && dateTimeUpdated) "$formattedDate, $formattedTime"
                    else if (selectedDiary != null) selectedDiaryDateTime
                    else "$formattedDate, $formattedTime",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            if (dateTimeUpdated) {

                IconButton(onClick = {

                    currentDate = LocalDate.now()
                    currentTime = LocalTime.now()
                    dateTimeUpdated = false
                    onUpdatedDateTime(
                        ZonedDateTime.of(
                            currentDate,
                            currentTime,
                            ZoneId.systemDefault()
                        )
                    )

                }) {
                    Icon(
                        imageVector = Icons.Default.Close, contentDescription = "close icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

            } else {
                IconButton(onClick = {
                    dateDialog.show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange, contentDescription = "date range",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedDiary != null) {
                DeleteDiaryAction(
                    selectedDiary = selectedDiary,
                    onDeleteClicked = onDeleteConfirmed
                )
            }
        }
    )
    CalendarDialog(
        state = dateDialog, selection = CalendarSelection.Date { localDate ->

            currentDate = localDate
            timeDialog.show()

        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )

    ClockDialog(state = timeDialog, selection = ClockSelection.HoursMinutes { hours, minutes ->

        currentTime = LocalTime.of(hours, minutes)
        dateTimeUpdated = true
        onUpdatedDateTime(
            ZonedDateTime.of(
                currentDate,
                currentTime,
                ZoneId.systemDefault()
            )
        )

    })
}

@Composable
fun DeleteDiaryAction(
    selectedDiary: Diary?,
    onDeleteClicked: () -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }
    var openDialog by remember {
        mutableStateOf(false)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {

        DropdownMenuItem(text = {
            Text(text = "Delete")
        }, onClick = {
            openDialog = true
            expanded = false
        })

    }

    DisplayAlertDialog(
        title = "Delete",
        message = "Are you sure you want to delete note '${selectedDiary?.title}'?",
        dialogOpened = openDialog,
        onCloseDialog = {
            openDialog = false
        },
        onYesClicked = onDeleteClicked
    )

    IconButton(onClick = { expanded = !expanded }) {
        Icon(
            imageVector = Icons.Default.MoreVert, contentDescription = "Overflow Menu Icon",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

}