package com.eventsnap.android.core.data.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.Recurrence
import com.eventsnap.android.core.model.TargetCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.TimeZone

class CalendarWriterImpl(
    private val context: Context,
) : CalendarWriter {
    override suspend fun writableCalendars(): List<TargetCalendar> =
        withContext(Dispatchers.IO) {
            val projection =
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.IS_PRIMARY,
                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                )
            val result = mutableListOf<TargetCalendar>()
            context.contentResolver
                .query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val accountCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                    val primaryCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.IS_PRIMARY)
                    val accessCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL)
                    while (cursor.moveToNext()) {
                        val accessLevel = cursor.getInt(accessCol)
                        if (accessLevel < CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) continue
                        result +=
                            TargetCalendar(
                                id = cursor.getLong(idCol),
                                displayName = cursor.getString(nameCol).orEmpty(),
                                accountName = cursor.getString(accountCol).orEmpty(),
                                isPrimary = cursor.getInt(primaryCol) == 1,
                            )
                    }
                }
            result
        }

    override suspend fun insertEvent(
        calendarId: Long,
        event: CalendarEvent,
    ): Long =
        withContext(Dispatchers.IO) {
            val values = buildEventValues(calendarId, event)
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.let { ContentUris.parseId(it) } ?: -1L

            if (eventId >= 0 && event.reminderMinutesBefore != null) {
                val reminderValues =
                    ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.MINUTES, event.reminderMinutesBefore)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
            }
            eventId
        }

    override suspend fun existingEventIds(candidateEventIds: List<Long>): Set<Long> =
        withContext(Dispatchers.IO) {
            val ids = candidateEventIds.filter { it > 0 }
            if (ids.isEmpty()) return@withContext emptySet()
            // Query the Events table for the ids we know about; deleted events won't come back.
            val placeholders = ids.joinToString(",") { "?" }
            val selection = "${CalendarContract.Events._ID} IN ($placeholders)"
            val args = ids.map { it.toString() }.toTypedArray()
            val found = mutableSetOf<Long>()
            context.contentResolver
                .query(
                    CalendarContract.Events.CONTENT_URI,
                    arrayOf(CalendarContract.Events._ID),
                    selection,
                    args,
                    null,
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
                    while (cursor.moveToNext()) found += cursor.getLong(idCol)
                }
            found
        }

    override suspend fun deleteEvents(eventIds: List<Long>) {
        withContext(Dispatchers.IO) {
            eventIds.filter { it > 0 }.forEach { id ->
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id)
                context.contentResolver.delete(uri, null, null)
            }
        }
    }

    /** Builds the CalendarProvider row for [event], handling all-day, recurrence, and timezone. */
    private fun buildEventValues(
        calendarId: Long,
        event: CalendarEvent,
    ): ContentValues {
        val rrule = rruleFor(event.recurrence)
        // All-day events must be stored at UTC midnight with EVENT_TIMEZONE=UTC, or calendar
        // apps can render them on the wrong day. Timed events keep the real timezone.
        val tz = if (event.allDay) "UTC" else (event.timeZoneId ?: TimeZone.getDefault().id)
        val startMillis = if (event.allDay) toUtcMidnight(event.startEpochMillis) else event.startEpochMillis
        val endMillis = if (event.allDay) toUtcMidnight(event.endEpochMillis) else event.endEpochMillis
        return ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.EVENT_TIMEZONE, tz)
            if (rrule != null) {
                // A recurring event uses DURATION instead of DTEND (RFC 5545 / CalendarProvider).
                val durationMillis = (endMillis - startMillis).coerceAtLeast(0)
                put(CalendarContract.Events.RRULE, rrule)
                put(CalendarContract.Events.DURATION, iso8601Duration(durationMillis, event.allDay))
            } else {
                put(CalendarContract.Events.DTEND, endMillis)
            }
            if (event.allDay) put(CalendarContract.Events.ALL_DAY, 1)
            event.location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
            event.description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
        }
    }

    /**
     * Converts a local-time instant to the UTC-midnight instant of the SAME calendar date.
     * All-day events are date-only, and CalendarProvider expects them at UTC midnight.
     */
    private fun toUtcMidnight(epochMillis: Long): Long {
        val localDate =
            java.time.Instant
                .ofEpochMilli(epochMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        return localDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    /** RRULE (RFC 5545) for a [Recurrence], or null for a one-off event. */
    private fun rruleFor(recurrence: Recurrence): String? =
        when (recurrence) {
            Recurrence.NONE -> null
            Recurrence.DAILY -> "FREQ=DAILY"
            Recurrence.WEEKLY -> "FREQ=WEEKLY"
            Recurrence.MONTHLY -> "FREQ=MONTHLY"
            Recurrence.YEARLY -> "FREQ=YEARLY"
        }

    /** ISO-8601 duration for a recurring event. All-day events use whole days (e.g. "P1D"). */
    private fun iso8601Duration(
        durationMillis: Long,
        allDay: Boolean,
    ): String {
        if (allDay) {
            val days = (durationMillis / 86_400_000L).coerceAtLeast(1)
            return "P${days}D"
        }
        val seconds = (durationMillis / 1000L).coerceAtLeast(1)
        return "PT${seconds}S"
    }
}
