package com.eventsnap.android.feature.review.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.Recurrence
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewState
import kotlinx.collections.immutable.persistentListOf
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

private val REMINDER_CHOICES: List<Pair<String, Int?>> =
    listOf(
        "No reminder" to null,
        "At time" to 0,
        "10 min before" to 10,
        "30 min before" to 30,
        "1 hour before" to 60,
        "1 day before" to 1440,
    )

private val RECURRENCE_CHOICES: List<Pair<String, Recurrence>> =
    listOf(
        "Does not repeat" to Recurrence.NONE,
        "Daily" to Recurrence.DAILY,
        "Weekly" to Recurrence.WEEKLY,
        "Monthly" to Recurrence.MONTHLY,
        "Yearly" to Recurrence.YEARLY,
    )

@Composable
fun ReviewScreenContent(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.md)) {
            Text("Review events", style = MaterialTheme.typography.titleLarge)

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                itemsIndexed(state.events, key = { index, _ -> index }) { index, event ->
                    EventCard(
                        index = index,
                        event = event,
                        // Selection checkbox only matters when there's more than one event to choose from.
                        showSelection = state.events.size > 1,
                        selected = state.selected.getOrElse(index) { true },
                        onAction = onAction,
                    )
                }

                if (state.calendars.isNotEmpty()) {
                    item {
                        Text(
                            "Add to calendar",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                    items(state.calendars, key = { it.id }) { calendar ->
                        CalendarOption(
                            calendar = calendar,
                            selected = state.selectedCalendarId == calendar.id,
                            onSelect = { onAction(ReviewAction.CalendarSelected(calendar.id)) },
                        )
                    }
                }
            }

            val error = state.error
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().testTag("review_error"),
                )
            }

            val checkedCount = state.checkedEvents.size
            Button(
                onClick = { onAction(ReviewAction.Confirm) },
                enabled = !state.isSaving && checkedCount > 0,
                modifier = Modifier.fillMaxWidth().testTag("review_confirm").padding(top = Spacing.sm),
            ) {
                val label =
                    when {
                        state.isSaving -> "Adding…"
                        checkedCount > 1 -> "Add $checkedCount to calendar"
                        else -> "Add to calendar"
                    }
                Text(label)
            }
        }
    }
}

@Composable
private fun EventCard(
    index: Int,
    event: CalendarEvent,
    showSelection: Boolean,
    selected: Boolean,
    onAction: (ReviewAction) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("event_card_$index")) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // When several events were extracted, let the user pick which ones to add.
                if (showSelection) {
                    Checkbox(
                        checked = selected,
                        onCheckedChange = { onAction(ReviewAction.SelectionToggled(index, it)) },
                        modifier = Modifier.testTag("select_checkbox_$index"),
                    )
                }
                OutlinedTextField(
                    value = event.title,
                    onValueChange = { onAction(ReviewAction.TitleChanged(index, it)) },
                    label = { Text("Title") },
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onAction(ReviewAction.RemoveEvent(index)) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove event")
                }
            }

            // Task checkbox: mark a to-do (something to DO) vs an event (something to attend).
            // Pre-checked from the AI's guess; the user confirms or corrects it. This is only a
            // classification — it doesn't change the time. Use the All-day switch for that.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = event.isTask,
                    onCheckedChange = { onAction(ReviewAction.TaskToggled(index, it)) },
                    modifier = Modifier.testTag("task_checkbox_$index"),
                )
                Column(modifier = Modifier.padding(start = Spacing.xs)) {
                    Text("This is a task", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "A to-do with a deadline (turn off All-day to give it a specific time)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // All-day switch — controls whether the event/task has a clock time, for both kinds.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("All-day", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = event.allDay,
                    onCheckedChange = { onAction(ReviewAction.AllDayToggled(index, it)) },
                    modifier = Modifier.testTag("allday_switch_$index"),
                )
            }

            // Start / end date+time editors
            DateTimeRow(
                label = "Starts",
                epochMillis = event.startEpochMillis,
                showTime = !event.allDay,
                onChanged = { onAction(ReviewAction.StartChanged(index, it)) },
            )
            DateTimeRow(
                label = "Ends",
                epochMillis = event.endEpochMillis,
                showTime = !event.allDay,
                onChanged = { onAction(ReviewAction.EndChanged(index, it)) },
            )

            OutlinedTextField(
                value = event.location.orEmpty(),
                onValueChange = { onAction(ReviewAction.LocationChanged(index, it)) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
            )

            ReminderDropdown(
                selected = event.reminderMinutesBefore,
                onSelected = { onAction(ReviewAction.ReminderChanged(index, it)) },
            )

            RecurrenceDropdown(
                selected = event.recurrence,
                onSelected = { onAction(ReviewAction.RecurrenceChanged(index, it)) },
            )
        }
    }
}

@Composable
private fun DateTimeRow(
    label: String,
    epochMillis: Long,
    showTime: Boolean,
    onChanged: (Long) -> Unit,
) {
    var showDate by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        AssistChip(onClick = { showDate = true }, label = { Text(formatDate(epochMillis)) })
        if (showTime) {
            AssistChip(
                onClick = { showTimeDialog = true },
                label = { Text(formatTime(epochMillis)) },
                modifier = Modifier.padding(start = Spacing.xs),
            )
        }
    }

    if (showDate) {
        DatePickerModal(
            initialMillis = epochMillis,
            onDismiss = { showDate = false },
            onConfirm = { pickedDateMillis ->
                showDate = false
                onChanged(mergeDate(epochMillis, pickedDateMillis))
            },
        )
    }
    if (showTimeDialog) {
        TimePickerModal(
            initialMillis = epochMillis,
            onDismiss = { showTimeDialog = false },
            onConfirm = { hour, minute ->
                showTimeDialog = false
                onChanged(mergeTime(epochMillis, hour, minute))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { pickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss() }) {
                Text("OK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        DatePicker(state = pickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerModal(
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val cal = Calendar.getInstance().apply { timeInMillis = initialMillis }
    val timeState =
        rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE),
            is24Hour = true,
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.md), contentAlignment = Alignment.Center) {
            TimePicker(state = timeState)
        }
    }
}

@Composable
private fun ReminderDropdown(
    selected: Int?,
    onSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = REMINDER_CHOICES.firstOrNull { it.second == selected }?.first ?: "No reminder"
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("Reminder: $label") },
            modifier = Modifier.testTag("reminder_chip"),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            REMINDER_CHOICES.forEach { (text, minutes) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelected(minutes)
                    },
                )
            }
        }
    }
}

@Composable
private fun RecurrenceDropdown(
    selected: Recurrence,
    onSelected: (Recurrence) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = RECURRENCE_CHOICES.firstOrNull { it.second == selected }?.first ?: "Does not repeat"
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text("Repeat: $label") },
            modifier = Modifier.testTag("recurrence_chip"),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            RECURRENCE_CHOICES.forEach { (text, value) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    },
                )
            }
        }
    }
}

@Composable
private fun CalendarOption(
    calendar: TargetCalendar,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onSelect)
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(
            "${calendar.displayName} (${calendar.accountName})",
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

private fun formatDate(millis: Long): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(millis))

private fun formatTime(millis: Long): String = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))

/** Replaces the date part of [baseMillis] with the date from [dateMillis], keeping the clock time. */
private fun mergeDate(
    baseMillis: Long,
    dateMillis: Long,
): Long {
    val base = Calendar.getInstance().apply { timeInMillis = baseMillis }
    val picked = Calendar.getInstance().apply { timeInMillis = dateMillis }
    picked.set(Calendar.HOUR_OF_DAY, base.get(Calendar.HOUR_OF_DAY))
    picked.set(Calendar.MINUTE, base.get(Calendar.MINUTE))
    picked.set(Calendar.SECOND, 0)
    picked.set(Calendar.MILLISECOND, 0)
    return picked.timeInMillis
}

/** Replaces the clock time of [baseMillis] with [hour]:[minute], keeping the date. */
private fun mergeTime(
    baseMillis: Long,
    hour: Int,
    minute: Int,
): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = baseMillis }
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@EventsnapPreviews
@Composable
private fun ReviewScreenContentPreview() {
    EventsnapTheme {
        ReviewScreenContent(
            state =
                ReviewState(
                    events =
                        persistentListOf(
                            CalendarEvent(
                                title = "Dinner with Ana",
                                startEpochMillis = 1_800_000_000_000L,
                                endEpochMillis = 1_800_003_600_000L,
                                location = "Osteria",
                                reminderMinutesBefore = 60,
                            ),
                        ),
                    calendars =
                        persistentListOf(
                            TargetCalendar(id = 1, displayName = "Personal", accountName = "me@gmail.com", isPrimary = true),
                        ),
                    selectedCalendarId = 1,
                ),
            onAction = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun ReviewScreenContentErrorPreview() {
    EventsnapTheme {
        ReviewScreenContent(
            state = ReviewState(error = "Pick a calendar first."),
            onAction = {},
        )
    }
}
