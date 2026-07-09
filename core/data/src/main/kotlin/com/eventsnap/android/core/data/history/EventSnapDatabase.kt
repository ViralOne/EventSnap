package com.eventsnap.android.core.data.history

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [EventHistoryEntity::class], version = 2, exportSchema = true)
abstract class EventSnapDatabase : RoomDatabase() {
    abstract fun eventHistoryDao(): EventHistoryDao

    companion object {
        /** v1 → v2: add calendarEventId so history rows can open/edit the real calendar event. */
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE event_history ADD COLUMN calendarEventId INTEGER NOT NULL DEFAULT -1")
                }
            }
    }
}
