package com.eventsnap.android.feature.review.data

import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar

interface ReviewRepository {
    /** Events handed over from the capture step (consumed once). */
    fun pendingEvents(): List<CalendarEvent>

    suspend fun writableCalendars(): List<TargetCalendar>

    /** Default target calendar id from settings, or null if none chosen yet. */
    suspend fun defaultCalendarId(): Long?

    /**
     * Writes [events] into the calendar with [calendarId] and records them in history.
     * Returns an [AddedBatch] that [undo] can use to remove exactly what was just added.
     */
    suspend fun confirm(
        calendarId: Long,
        events: List<CalendarEvent>,
    ): AddedBatch

    /** Reverses a [batch] created by [confirm]: deletes the calendar events and history rows. */
    suspend fun undo(batch: AddedBatch)
}

/** The calendar event ids + history row ids created by one [ReviewRepository.confirm] call. */
data class AddedBatch(
    val calendarEventIds: List<Long>,
    val historyIds: List<Long>,
)
