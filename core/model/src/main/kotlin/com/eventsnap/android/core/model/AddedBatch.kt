package com.eventsnap.android.core.model

/**
 * The calendar event ids + history row ids created by one confirm/add action. Held after adding so
 * an "Undo" can remove exactly what was just written (the events from the CalendarProvider and the
 * matching rows from local history).
 */
data class AddedBatch(
    val calendarEventIds: List<Long>,
    val historyIds: List<Long>,
)
