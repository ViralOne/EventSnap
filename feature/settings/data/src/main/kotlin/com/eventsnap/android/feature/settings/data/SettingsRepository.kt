package com.eventsnap.android.feature.settings.data

import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val groqApiKey: Flow<String?>
    val defaultCalendarId: Flow<Long?>
    val defaultReminderMinutes: Flow<Int>
    val themePreference: Flow<ThemePreference>
    val dynamicColor: Flow<Boolean>

    /** Pinned model per arm; null means automatic selection from Groq's live list. */
    val textModel: Flow<String?>
    val visionModel: Flow<String?>

    suspend fun writableCalendars(): List<TargetCalendar>

    /** Models Groq serves right now. Empty when the list can't be fetched (no key, offline). */
    suspend fun availableModels(forceRefresh: Boolean = false): List<AiModel>

    /** The model id that would actually be used for [arm] with the current settings. */
    suspend fun resolvedModel(arm: AiModelArm): String

    suspend fun setGroqApiKey(key: String)

    /** Pass null for automatic selection. */
    suspend fun setModel(
        arm: AiModelArm,
        modelId: String?,
    )

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)

    suspend fun setThemePreference(preference: ThemePreference)

    suspend fun setDynamicColor(enabled: Boolean)
}
