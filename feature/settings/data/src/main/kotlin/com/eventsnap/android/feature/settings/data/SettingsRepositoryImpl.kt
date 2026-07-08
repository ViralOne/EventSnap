package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.TargetCalendar

internal class SettingsRepositoryImpl(
    private val settingsStore: SettingsStore,
    private val calendarWriter: CalendarWriter,
) : SettingsRepository {
    override val groqApiKey = settingsStore.groqApiKey
    override val defaultCalendarId = settingsStore.defaultCalendarId
    override val defaultReminderMinutes = settingsStore.defaultReminderMinutes

    override suspend fun writableCalendars(): List<TargetCalendar> = calendarWriter.writableCalendars()

    override suspend fun setGroqApiKey(key: String) = settingsStore.setGroqApiKey(key)

    override suspend fun setDefaultCalendarId(id: Long) = settingsStore.setDefaultCalendarId(id)

    override suspend fun setDefaultReminderMinutes(minutes: Int) = settingsStore.setDefaultReminderMinutes(minutes)
}
