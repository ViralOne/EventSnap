package com.eventsnap.android.core.data.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.eventsnap.android.core.model.CalendarEvent
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
            val tz = event.timeZoneId ?: TimeZone.getDefault().id
            val values =
                ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, event.title)
                    put(CalendarContract.Events.DTSTART, event.startEpochMillis)
                    put(CalendarContract.Events.DTEND, event.endEpochMillis)
                    put(CalendarContract.Events.EVENT_TIMEZONE, tz)
                    if (event.allDay) put(CalendarContract.Events.ALL_DAY, 1)
                    event.location?.let { put(CalendarContract.Events.EVENT_LOCATION, it) }
                    event.description?.let { put(CalendarContract.Events.DESCRIPTION, it) }
                }
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
}
