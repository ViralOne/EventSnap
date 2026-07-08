package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.model.TargetCalendar
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val groqApiKey: Flow<String?>
    val defaultCalendarId: Flow<Long?>
    val defaultReminderMinutes: Flow<Int>

    suspend fun writableCalendars(): List<TargetCalendar>

    suspend fun setGroqApiKey(key: String)

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)
}
