package com.eventsnap.android.feature.review.mvi

import com.eventsnap.android.core.ViewAction
import com.eventsnap.android.core.ViewSideEffect
import com.eventsnap.android.core.ViewState
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ReviewState(
    val events: ImmutableList<CalendarEvent> = persistentListOf(),
    val calendars: ImmutableList<TargetCalendar> = persistentListOf(),
    val selectedCalendarId: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
) : ViewState

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

    data class RemoveEvent(
        val index: Int,
    ) : ReviewAction

    data class CalendarSelected(
        val calendarId: Long,
    ) : ReviewAction

    data object Confirm : ReviewAction

    data object ErrorDismissed : ReviewAction
}

sealed interface ReviewEffect : ViewSideEffect {
    data class ShowSaved(
        val count: Int,
    ) : ReviewEffect

    data object NavigateBackToCapture : ReviewEffect
}
