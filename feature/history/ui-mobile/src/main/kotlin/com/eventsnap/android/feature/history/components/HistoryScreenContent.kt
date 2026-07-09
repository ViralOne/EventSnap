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
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onEditEvent: (Long) -> Unit,
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
                HistoryRow(item = item, onOpenEvent = onOpenEvent, onEditEvent = onEditEvent)
            }
        }
    }
}

@Composable
private fun HistoryRow(
    item: HistoryItem,
    onOpenEvent: (Long) -> Unit,
    onEditEvent: (Long) -> Unit,
) {
    val openable = item.calendarEventId > 0
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
            Column(modifier = Modifier.weight(1f)) {
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
            }
            if (openable) {
                IconButton(
                    onClick = { onEditEvent(item.calendarEventId) },
                    modifier = Modifier.testTag("history_edit_${item.id}"),
                ) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = "Edit in calendar")
                }
            }
        }
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
                        ),
                ),
            onOpenEvent = {},
            onEditEvent = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun HistoryScreenContentEmptyPreview() {
    EventsnapTheme {
        HistoryScreenContent(state = HistoryState(isLoading = false), onOpenEvent = {}, onEditEvent = {})
    }
}
