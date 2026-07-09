package com.eventsnap.android.feature.history.data

import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.history.EventHistoryDao
import com.eventsnap.android.core.model.CalendarEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class HistoryRepositoryImpl(
    private val dao: EventHistoryDao,
    private val calendarWriter: CalendarWriter,
) : HistoryRepository {
    override fun observeHistory(): Flow<List<HistoryItem>> =
        dao.observeAll().map { entities ->
            // Check which of the tracked calendar events still exist, so we can flag deleted ones.
            val trackedIds = entities.map { it.calendarEventId }.filter { it > 0 }
            val existing = calendarWriter.existingEventIds(trackedIds)
            entities.map { entity ->
                HistoryItem(
                    id = entity.id,
                    title = entity.title,
                    startEpochMillis = entity.startEpochMillis,
                    allDay = entity.allDay,
                    location = entity.location,
                    calendarEventId = entity.calendarEventId,
                    createdAtEpochMillis = entity.createdAtEpochMillis,
                    // Only rows with a real tracked id can be known-deleted; older -1 rows stay as-is.
                    deletedFromCalendar = entity.calendarEventId > 0 && entity.calendarEventId !in existing,
                )
            }
        }

    override suspend fun restoreEvent(historyId: Long) {
        val entity = dao.getById(historyId) ?: return
        val event =
            CalendarEvent(
                title = entity.title,
                startEpochMillis = entity.startEpochMillis,
                endEpochMillis = entity.endEpochMillis,
                allDay = entity.allDay,
                location = entity.location,
                description = entity.description,
                reminderMinutesBefore = entity.reminderMinutesBefore,
            )
        val newId = calendarWriter.insertEvent(entity.calendarId, event)
        if (newId > 0) dao.updateCalendarEventId(historyId, newId)
    }
}
