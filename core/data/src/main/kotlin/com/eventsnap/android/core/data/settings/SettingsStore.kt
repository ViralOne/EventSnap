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

    suspend fun setGroqApiKey(key: String)

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)

    suspend fun setThemePreference(preference: ThemePreference)

    suspend fun setDynamicColor(enabled: Boolean)
}
