package com.eventsnap.android.feature.review.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewState
import kotlinx.collections.immutable.persistentListOf
import java.text.DateFormat
import java.util.Date

@Composable
fun ReviewScreenContent(
    state: ReviewState,
    onAction: (ReviewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(Spacing.md)) {
        Text("Review events", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            itemsIndexed(state.events, key = { index, _ -> index }) { index, event ->
                EventCard(
                    index = index,
                    event = event,
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

        Button(
            onClick = { onAction(ReviewAction.Confirm) },
            enabled = !state.isSaving && state.events.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().testTag("review_confirm").padding(top = Spacing.sm),
        ) {
            Text(if (state.isSaving) "Adding…" else "Add to calendar")
        }
    }
}

@Composable
private fun EventCard(
    index: Int,
    event: CalendarEvent,
    onAction: (ReviewAction) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("event_card_$index")) {
        Column(modifier = Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = formatRange(event),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = event.location.orEmpty(),
                onValueChange = { onAction(ReviewAction.LocationChanged(index, it)) },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
            )
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

private fun formatRange(event: CalendarEvent): String {
    val fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    return "${fmt.format(Date(event.startEpochMillis))} – ${fmt.format(Date(event.endEpochMillis))}"
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
