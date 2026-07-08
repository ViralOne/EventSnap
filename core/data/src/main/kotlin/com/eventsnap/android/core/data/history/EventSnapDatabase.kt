package com.eventsnap.android.core.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EventHistoryEntity::class], version = 1, exportSchema = true)
abstract class EventSnapDatabase : RoomDatabase() {
    abstract fun eventHistoryDao(): EventHistoryDao
}
