package com.eventsnap.android.feature.review

import androidx.lifecycle.viewModelScope
import com.eventsnap.android.core.BaseViewModel
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.feature.review.data.ReviewRepository
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewEffect
import com.eventsnap.android.feature.review.mvi.ReviewState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repository: ReviewRepository,
) : BaseViewModel<ReviewState, ReviewAction, ReviewEffect>(ReviewState()) {
    override suspend fun onAction(action: ReviewAction) {
        when (action) {
            is ReviewAction.Load -> load()
            is ReviewAction.RemoveEvent -> {
                // Drop the event and its aligned selection flag together.
                setState {
                    copy(
                        events = events.filterIndexed { i, _ -> i != action.index }.toImmutableList(),
                        selected = selected.filterIndexed { i, _ -> i != action.index }.toImmutableList(),
                    )
                }
                // Nothing left to review → there's no screen to show, so go back to Capture.
                if (state.value.events.isEmpty()) setEffect(ReviewEffect.NavigateBackToCapture)
            }
            is ReviewAction.SelectionToggled ->
                setState {
                    copy(selected = selected.mapIndexed { i, s -> if (i == action.index) action.selected else s }.toImmutableList())
                }
            is ReviewAction.CalendarSelected -> setState { copy(selectedCalendarId = action.calendarId) }
            is ReviewAction.ErrorDismissed -> setState { copy(error = null) }
            is ReviewAction.Confirm -> confirm()
            is ReviewAction.Undo ->
                viewModelScope.launch {
                    runCatching { repository.undo(action.batch) }
                        .onFailure { throwable -> setState { copy(error = throwable.message ?: "Could not undo.") } }
                }
            else -> onFieldEdit(action)
        }
    }

    /** Per-event field edits from the review cards; kept separate to keep [onAction] simple. */
    private fun onFieldEdit(action: ReviewAction) {
        when (action) {
            is ReviewAction.TitleChanged -> mutateEvent(action.index) { it.copy(title = action.value) }
            is ReviewAction.LocationChanged -> mutateEvent(action.index) { it.copy(location = action.value) }
            is ReviewAction.StartChanged ->
                mutateEvent(action.index) { event ->
                    // Keep the duration stable when the start moves; never let end precede start.
                    val duration = (event.endEpochMillis - event.startEpochMillis).coerceAtLeast(0)
                    event.copy(startEpochMillis = action.epochMillis, endEpochMillis = action.epochMillis + duration)
                }
            is ReviewAction.EndChanged ->
                mutateEvent(action.index) { event ->
                    event.copy(endEpochMillis = action.epochMillis.coerceAtLeast(event.startEpochMillis))
                }
            is ReviewAction.AllDayToggled -> mutateEvent(action.index) { it.copy(allDay = action.allDay) }
            is ReviewAction.TaskToggled ->
                // "Task" is just a classification (to-do vs event); it's independent of all-day, so
                // toggling it never touches the time. The user controls all-day with its own switch.
                mutateEvent(action.index) { it.copy(isTask = action.isTask) }
            is ReviewAction.ReminderChanged -> mutateEvent(action.index) { it.copy(reminderMinutesBefore = action.minutesBefore) }
            is ReviewAction.RecurrenceChanged -> mutateEvent(action.index) { it.copy(recurrence = action.recurrence) }
            else -> Unit
        }
    }

    /**
     * Reloads pending events + calendars from scratch. Dispatched on every screen entry so a
     * repeat visit never shows the previous batch (the ViewModel may be reused across visits).
     */
    private fun load() {
        val pending = repository.pendingEvents()
        // Full reset — clears any events/selection/error left over from a prior visit.
        // Everything starts checked; the user can untick any they don't want.
        setState {
            ReviewState(
                events = pending.toImmutableList(),
                selected = pending.map { true }.toImmutableList(),
            )
        }
        viewModelScope.launch {
            runCatching {
                val calendars = repository.writableCalendars()
                val default =
                    repository.defaultCalendarId()
                        ?: calendars.firstOrNull { it.isPrimary }?.id
                        ?: calendars.firstOrNull()?.id
                calendars to default
            }.onSuccess { (calendars, default) ->
                setState { copy(calendars = calendars.toImmutableList(), selectedCalendarId = default) }
            }.onFailure { throwable ->
                setState { copy(error = throwable.message ?: "Could not read your calendars.") }
            }
        }
    }

    private fun mutateEvent(
        index: Int,
        transform: (CalendarEvent) -> CalendarEvent,
    ) {
        setState {
            copy(events = events.mapIndexed { i, e -> if (i == index) transform(e) else e }.toImmutableList())
        }
    }

    private fun confirm() {
        val current = state.value
        val calendarId = current.selectedCalendarId
        if (calendarId == null) {
            setState { copy(error = "Pick a calendar first.") }
            return
        }
        val toAdd = current.checkedEvents
        if (toAdd.isEmpty()) {
            setState { copy(error = "Select at least one event to add.") }
            return
        }
        setState { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.confirm(calendarId, toAdd) }
                .onSuccess { batch ->
                    setState { copy(isSaving = false) }
                    // The screen shows a "saved · Undo" snackbar and navigates back when it resolves,
                    // so we don't emit a navigate effect here.
                    setEffect(ReviewEffect.ShowSaved(toAdd.size, batch))
                }.onFailure { throwable ->
                    setState { copy(isSaving = false, error = throwable.message ?: "Could not add the events.") }
                }
        }
    }
}
