package com.eventsnap.android.core.data.calendar

import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar

/**
 * Reads writable calendars and inserts events (with reminders) through the Android
 * CalendarProvider. Requires READ_CALENDAR / WRITE_CALENDAR to be granted before use.
 */
interface CalendarWriter {
    suspend fun writableCalendars(): List<TargetCalendar>

    /** Inserts [event] into the calendar with [calendarId]. Returns the new event's id. */
    suspend fun insertEvent(
        calendarId: Long,
        event: CalendarEvent,
    ): Long

    /**
     * Returns which of [candidateEventIds] still exist (not deleted) in the CalendarProvider.
     * Used to detect history rows whose calendar event the user has since deleted.
     */
    suspend fun existingEventIds(candidateEventIds: List<Long>): Set<Long>

    /** Deletes the calendar events with [eventIds] (used to undo a just-added batch). */
    suspend fun deleteEvents(eventIds: List<Long>)
}
