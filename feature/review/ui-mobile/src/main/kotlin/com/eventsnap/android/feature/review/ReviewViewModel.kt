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
                mutateEvent(action.index) { event ->
                    // A task is a to-do with a deadline, not a time slot, so it's always all-day.
                    event.copy(isTask = action.isTask, allDay = if (action.isTask) true else event.allDay)
                }
            is ReviewAction.ReminderChanged -> mutateEvent(action.index) { it.copy(reminderMinutesBefore = action.minutesBefore) }
            is ReviewAction.RemoveEvent ->
                setState {
                    copy(events = events.filterIndexed { i, _ -> i != action.index }.toImmutableList())
                }
            is ReviewAction.CalendarSelected -> setState { copy(selectedCalendarId = action.calendarId) }
            is ReviewAction.ErrorDismissed -> setState { copy(error = null) }
            is ReviewAction.Confirm -> confirm()
        }
    }

    /**
     * Reloads pending events + calendars from scratch. Dispatched on every screen entry so a
     * repeat visit never shows the previous batch (the ViewModel may be reused across visits).
     */
    private fun load() {
        val pending = repository.pendingEvents()
        // Full reset — clears any events/selection/error left over from a prior visit.
        setState { ReviewState(events = pending.toImmutableList()) }
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
        if (current.events.isEmpty()) {
            setState { copy(error = "Nothing to add.") }
            return
        }
        setState { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.confirm(calendarId, current.events) }
                .onSuccess {
                    setState { copy(isSaving = false) }
                    setEffect(ReviewEffect.ShowSaved(current.events.size))
                    setEffect(ReviewEffect.NavigateBackToCapture)
                }.onFailure { throwable ->
                    setState { copy(isSaving = false, error = throwable.message ?: "Could not add the events.") }
                }
        }
    }
}
