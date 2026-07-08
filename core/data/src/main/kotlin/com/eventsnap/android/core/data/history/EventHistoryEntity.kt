package com.eventsnap.android.core.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_history")
data class EventHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val allDay: Boolean,
    val location: String?,
    val description: String?,
    val reminderMinutesBefore: Int?,
    val calendarId: Long,
    val createdAtEpochMillis: Long,
)
