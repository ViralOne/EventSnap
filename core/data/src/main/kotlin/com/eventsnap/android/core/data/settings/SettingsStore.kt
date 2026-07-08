package com.eventsnap.android.core.data.settings

import kotlinx.coroutines.flow.Flow

/**
 * Persistent app settings. The Groq API key is held in EncryptedSharedPreferences; the rest
 * (default calendar id, default reminder) live in the same encrypted store for simplicity.
 */
interface SettingsStore {
    val groqApiKey: Flow<String?>
    val defaultCalendarId: Flow<Long?>
    val defaultReminderMinutes: Flow<Int>

    suspend fun setGroqApiKey(key: String)

    suspend fun setDefaultCalendarId(id: Long)

    suspend fun setDefaultReminderMinutes(minutes: Int)
}
