package com.eventsnap.android.feature.history.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.feature.history.data.HistoryItem
import com.eventsnap.android.feature.history.mvi.HistoryState
import kotlinx.collections.immutable.persistentListOf
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreenContent(
    state: HistoryState,
    onOpenEvent: (Long) -> Unit,
    onRestoreEvent: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(Spacing.md)) {
        Text("History", style = MaterialTheme.typography.titleLarge)

        if (!state.isLoading && state.items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "No events yet.\nEvents you add to your calendar will show up here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("history_empty"),
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(state.items, key = { it.id }) { item ->
                HistoryRow(item = item, onOpenEvent = onOpenEvent, onRestoreEvent = onRestoreEvent)
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    onOpenEvent: (Long) -> Unit,
    onRestoreEvent: (Long) -> Unit,
) {
    // A deleted event can't be opened; a row without a tracked id (older data) also isn't openable.
    val openable = item.calendarEventId > 0 && !item.deletedFromCalendar
    // Dim the whole row when its calendar event was deleted.
    val contentAlpha = if (item.deletedFromCalendar) 0.5f else 1f
    var showRestoreDialog by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("history_row_${item.id}")
                .then(
                    if (openable) Modifier.clickable { onOpenEvent(item.calendarEventId) } else Modifier,
                ),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).alpha(contentAlpha)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatWhen(item),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val location = item.location
                if (!location.isNullOrBlank()) {
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.deletedFromCalendar) {
                    Text(
                        text = "Deleted from calendar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("history_deleted_${item.id}"),
                    )
                }
            }
            // Deleted rows get an enabled "+" to re-add the event to the calendar.
            if (item.deletedFromCalendar) {
                IconButton(
                    onClick = { showRestoreDialog = true },
                    modifier = Modifier.testTag("history_restore_${item.id}"),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add back to calendar")
                }
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Add event back?") },
            text = { Text("Add \"${item.title}\" back to your calendar?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreDialog = false
                        onRestoreEvent(item.id)
                    },
                    modifier = Modifier.testTag("history_restore_confirm_${item.id}"),
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            },
        )
    }
}

private fun formatWhen(item: HistoryItem): String {
    val date = Date(item.startEpochMillis)
    return if (item.allDay) {
        DateFormat.getDateInstance(DateFormat.MEDIUM).format(date) + " · all-day"
    } else {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date)
    }
}

@EventsnapPreviews
@Composable
private fun HistoryScreenContentPreview() {
    EventsnapTheme {
        HistoryScreenContent(
            state =
                HistoryState(
                    isLoading = false,
                    items =
                        persistentListOf(
                            HistoryItem(1, "Dinner with Ana", 1_800_000_000_000L, false, "Osteria", 101, 1_800_000_000_000L),
                            HistoryItem(2, "Ramona's birthday", 1_800_500_000_000L, true, null, 102, 1_800_500_000_000L),
                            HistoryItem(
                                3,
                                "Old meeting",
                                1_800_900_000_000L,
                                false,
                                null,
                                103,
                                1_800_900_000_000L,
                                deletedFromCalendar = true,
                            ),
                        ),
                ),
            onOpenEvent = {},
            onRestoreEvent = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun HistoryScreenContentEmptyPreview() {
    EventsnapTheme {
        HistoryScreenContent(state = HistoryState(isLoading = false), onOpenEvent = {}, onRestoreEvent = {})
    }
}
