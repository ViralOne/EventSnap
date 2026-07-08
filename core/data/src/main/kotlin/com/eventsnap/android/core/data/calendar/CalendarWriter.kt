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
}
