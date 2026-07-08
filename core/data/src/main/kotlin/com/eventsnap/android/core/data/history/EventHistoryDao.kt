package com.eventsnap.android.core.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventHistoryDao {
    @Query("SELECT * FROM event_history ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<EventHistoryEntity>>

    @Insert
    suspend fun insert(entity: EventHistoryEntity): Long
}
