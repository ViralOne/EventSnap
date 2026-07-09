package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val groqApiKey: Flow<String?>
    val defaultCalendarId: Flow<Long?>
    val defaultReminderMinutes: Flow<Int>
    val themePreference: Flow<ThemePreference>
    val dynamicColor: Flow<Boolean>

    suspend fun writableCalendars(): List<TargetCalendar>

    suspend fun setGroqApiKey(key: String)

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)

    suspend fun setThemePreference(preference: ThemePreference)

    suspend fun setDynamicColor(enabled: Boolean)
}
