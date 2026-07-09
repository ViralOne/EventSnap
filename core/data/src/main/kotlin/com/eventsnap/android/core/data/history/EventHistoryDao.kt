package com.eventsnap.android.core.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventHistoryDao {
    @Query("SELECT * FROM event_history ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<EventHistoryEntity>>

    @Query("SELECT * FROM event_history WHERE id = :id")
    suspend fun getById(id: Long): EventHistoryEntity?

    @Insert
    suspend fun insert(entity: EventHistoryEntity): Long

    /** Repoints a history row at a freshly re-created calendar event (used when restoring a deleted one). */
    @Query("UPDATE event_history SET calendarEventId = :newCalendarEventId WHERE id = :id")
    suspend fun updateCalendarEventId(
        id: Long,
        newCalendarEventId: Long,
    )

    /** Removes history rows by id (used to undo a just-added batch). */
    @Query("DELETE FROM event_history WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
