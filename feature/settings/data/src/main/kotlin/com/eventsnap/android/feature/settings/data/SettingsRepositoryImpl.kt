package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.groq.GroqModelRegistry
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference

internal class SettingsRepositoryImpl(
    private val settingsStore: SettingsStore,
    private val calendarWriter: CalendarWriter,
    private val modelRegistry: GroqModelRegistry,
) : SettingsRepository {
    override val groqApiKey = settingsStore.groqApiKey
    override val defaultCalendarId = settingsStore.defaultCalendarId
    override val defaultReminderMinutes = settingsStore.defaultReminderMinutes
    override val themePreference = settingsStore.themePreference
    override val dynamicColor = settingsStore.dynamicColor
    override val textModel = settingsStore.textModel
    override val visionModel = settingsStore.visionModel

    override suspend fun writableCalendars(): List<TargetCalendar> = calendarWriter.writableCalendars()

    override suspend fun availableModels(forceRefresh: Boolean): List<AiModel> = modelRegistry.availableModels(forceRefresh)

    override suspend fun resolvedModel(arm: AiModelArm): String = modelRegistry.resolve(arm)

    override suspend fun setGroqApiKey(key: String) = settingsStore.setGroqApiKey(key)

    override suspend fun setModel(
        arm: AiModelArm,
        modelId: String?,
    ) = when (arm) {
        AiModelArm.TEXT -> settingsStore.setTextModel(modelId)
        AiModelArm.VISION -> settingsStore.setVisionModel(modelId)
    }

    override suspend fun setDefaultCalendarId(id: Long) = settingsStore.setDefaultCalendarId(id)

    override suspend fun setDefaultReminderMinutes(minutes: Int) = settingsStore.setDefaultReminderMinutes(minutes)

    override suspend fun setThemePreference(preference: ThemePreference) = settingsStore.setThemePreference(preference)

    override suspend fun setDynamicColor(enabled: Boolean) = settingsStore.setDynamicColor(enabled)
}
