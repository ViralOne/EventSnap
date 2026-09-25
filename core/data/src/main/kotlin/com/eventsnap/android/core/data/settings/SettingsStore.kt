package com.eventsnap.android.core.data.settings

import com.eventsnap.android.core.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * Persistent app settings. The Groq API key is held in EncryptedSharedPreferences; the rest
 * (default calendar id, default reminder, theme) live in the same encrypted store for simplicity.
 */
interface SettingsStore {
    val groqApiKey: Flow<String?>
    val defaultCalendarId: Flow<Long?>
    val defaultReminderMinutes: Flow<Int>
    val themePreference: Flow<ThemePreference>
    val dynamicColor: Flow<Boolean>

    /** Model pinned for typed descriptions; null means "let the app pick a live one". */
    val textModel: Flow<String?>

    /** Model pinned for photos/PDFs; null means "let the app pick a live one". */
    val visionModel: Flow<String?>

    suspend fun setGroqApiKey(key: String)

    /** Pass null to go back to automatic selection against Groq's live model list. */
    suspend fun setTextModel(modelId: String?)

    /** Pass null to go back to automatic selection against Groq's live model list. */
    suspend fun setVisionModel(modelId: String?)

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)

    suspend fun setThemePreference(preference: ThemePreference)

    suspend fun setDynamicColor(enabled: Boolean)
}
