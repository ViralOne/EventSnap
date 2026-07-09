package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference

internal class SettingsRepositoryImpl(
    private val settingsStore: SettingsStore,
    private val calendarWriter: CalendarWriter,
) : SettingsRepository {
    override val groqApiKey = settingsStore.groqApiKey
    override val defaultCalendarId = settingsStore.defaultCalendarId
    override val defaultReminderMinutes = settingsStore.defaultReminderMinutes
    override val themePreference = settingsStore.themePreference
    override val dynamicColor = settingsStore.dynamicColor

    override suspend fun writableCalendars(): List<TargetCalendar> = calendarWriter.writableCalendars()

    override suspend fun setGroqApiKey(key: String) = settingsStore.setGroqApiKey(key)

    override suspend fun setDefaultCalendarId(id: Long) = settingsStore.setDefaultCalendarId(id)

    override suspend fun setDefaultReminderMinutes(minutes: Int) = settingsStore.setDefaultReminderMinutes(minutes)

    override suspend fun setThemePreference(preference: ThemePreference) = settingsStore.setThemePreference(preference)

    override suspend fun setDynamicColor(enabled: Boolean) = settingsStore.setDynamicColor(enabled)
}
