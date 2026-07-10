package com.eventsnap.android.feature.review.mvi

import com.eventsnap.android.core.ViewAction
import com.eventsnap.android.core.ViewSideEffect
import com.eventsnap.android.core.ViewState
import com.eventsnap.android.core.model.AddedBatch
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.PlaceSuggestion
import com.eventsnap.android.core.model.Recurrence
import com.eventsnap.android.core.model.TargetCalendar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ReviewState(
    val events: ImmutableList<CalendarEvent> = persistentListOf(),
    /** One flag per event (aligned by index): whether it will be added on confirm. */
    val selected: ImmutableList<Boolean> = persistentListOf(),
    val calendars: ImmutableList<TargetCalendar> = persistentListOf(),
    val selectedCalendarId: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    /** Index of the event whose Location field is being typed in (drives which card shows the dropdown). */
    val locationQueryIndex: Int? = null,
    /** Place suggestions for the currently-edited location field. */
    val locationSuggestions: ImmutableList<PlaceSuggestion> = persistentListOf(),
) : ViewState {
    /** The events the user has ticked to add. */
    val checkedEvents: List<CalendarEvent>
        get() = events.filterIndexed { i, _ -> selected.getOrElse(i) { true } }
}

sealed interface ReviewAction : ViewAction {
    data object Load : ReviewAction

    data class TitleChanged(
        val index: Int,
        val value: String,
    ) : ReviewAction

    data class LocationChanged(
        val index: Int,
        val value: String,
    ) : ReviewAction

    /** User tapped a place suggestion; store its clean address and close the dropdown. */
    data class LocationSuggestionPicked(
        val index: Int,
        val suggestion: PlaceSuggestion,
    ) : ReviewAction

    /** Location field lost focus / dropdown dismissed — clear suggestions. */
    data object LocationSearchDismissed : ReviewAction

    /** New start instant (epoch millis) picked from the date/time pickers. */
    data class StartChanged(
        val index: Int,
        val epochMillis: Long,
    ) : ReviewAction

    /** New end instant (epoch millis). */
    data class EndChanged(
        val index: Int,
        val epochMillis: Long,
    ) : ReviewAction

    data class AllDayToggled(
        val index: Int,
        val allDay: Boolean,
    ) : ReviewAction

    data class TaskToggled(
        val index: Int,
        val isTask: Boolean,
    ) : ReviewAction

    data class ReminderChanged(
        val index: Int,
        val minutesBefore: Int?,
    ) : ReviewAction

    data class RecurrenceChanged(
        val index: Int,
        val recurrence: Recurrence,
    ) : ReviewAction

    data class RemoveEvent(
        val index: Int,
    ) : ReviewAction

    /** Tick/untick whether an extracted event will be added on confirm. */
    data class SelectionToggled(
        val index: Int,
        val selected: Boolean,
    ) : ReviewAction

    data class CalendarSelected(
        val calendarId: Long,
    ) : ReviewAction

    data object Confirm : ReviewAction

    /** Undo the just-added batch (delete the calendar events + history rows). */
    data class Undo(
        val batch: AddedBatch,
    ) : ReviewAction

    data object ErrorDismissed : ReviewAction
}

sealed interface ReviewEffect : ViewSideEffect {
    /** Events were added; carries the batch so the UI can offer an Undo. */
    data class ShowSaved(
        val count: Int,
        val batch: AddedBatch,
    ) : ReviewEffect

    data object NavigateBackToCapture : ReviewEffect
}
