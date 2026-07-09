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
)

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryItem>>
}
