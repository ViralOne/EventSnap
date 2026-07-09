package com.eventsnap.android.feature.history.data

import kotlinx.coroutines.flow.Flow

/** A previously created event, for the history list. */
data class HistoryItem(
    val id: Long,
    val title: String,
    val startEpochMillis: Long,
    val allDay: Boolean,
    val location: String?,
    /** CalendarProvider event id, or -1 if this row predates event-id tracking. */
    val calendarEventId: Long,
    val createdAtEpochMillis: Long,
    /** True when this row's calendar event has been deleted from the device calendar. */
    val deletedFromCalendar: Boolean = false,
)

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryItem>>

    /**
     * Re-creates the calendar event for a history row whose event was deleted, using the stored
     * details, and repoints the row at the new event so it becomes valid again.
     */
    suspend fun restoreEvent(historyId: Long)
}
