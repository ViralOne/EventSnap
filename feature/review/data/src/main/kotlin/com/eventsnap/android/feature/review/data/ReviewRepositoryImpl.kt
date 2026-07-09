package com.eventsnap.android.feature.review.data

import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.handoff.ExtractedEventsHolder
import com.eventsnap.android.core.data.history.EventHistoryDao
import com.eventsnap.android.core.data.history.EventHistoryEntity
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.TargetCalendar
import kotlinx.coroutines.flow.first

internal class ReviewRepositoryImpl(
    private val extractedEventsHolder: ExtractedEventsHolder,
    private val calendarWriter: CalendarWriter,
    private val settingsStore: SettingsStore,
    private val historyDao: EventHistoryDao,
) : ReviewRepository {
    override fun pendingEvents(): List<CalendarEvent> = extractedEventsHolder.consume()

    override suspend fun writableCalendars(): List<TargetCalendar> = calendarWriter.writableCalendars()

    override suspend fun defaultCalendarId(): Long? = settingsStore.defaultCalendarId.first()

    // Catches Throwable deliberately: any failure mid-batch must trigger rollback, then rethrow.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun confirm(
        calendarId: Long,
        events: List<CalendarEvent>,
    ): AddedBatch {
        val now = System.currentTimeMillis()
        val calendarEventIds = mutableListOf<Long>()
        val historyIds = mutableListOf<Long>()
        try {
            events.forEach { event ->
                val calendarEventId = calendarWriter.insertEvent(calendarId, event)
                calendarEventIds += calendarEventId
                val historyId =
                    historyDao.insert(
                        EventHistoryEntity(
                            title = event.title,
                            startEpochMillis = event.startEpochMillis,
                            endEpochMillis = event.endEpochMillis,
                            allDay = event.allDay,
                            location = event.location,
                            description = event.description,
                            reminderMinutesBefore = event.reminderMinutesBefore,
                            calendarId = calendarId,
                            calendarEventId = calendarEventId,
                            createdAtEpochMillis = now,
                        ),
                    )
                historyIds += historyId
            }
        } catch (t: Throwable) {
            // A mid-batch failure would leave partial data; roll back what we already created.
            runCatching { calendarWriter.deleteEvents(calendarEventIds) }
            runCatching { historyDao.deleteByIds(historyIds) }
            throw t
        }
        return AddedBatch(calendarEventIds = calendarEventIds, historyIds = historyIds)
    }

    override suspend fun undo(batch: AddedBatch) {
        calendarWriter.deleteEvents(batch.calendarEventIds)
        historyDao.deleteByIds(batch.historyIds)
    }
}
