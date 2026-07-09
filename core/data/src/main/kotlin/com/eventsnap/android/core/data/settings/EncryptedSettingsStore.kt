// androidx.security.crypto (EncryptedSharedPreferences / MasterKey) is deprecated by Google with
// no in-Jetpack replacement yet (the successor library is still alpha). It remains the standard,
// working way to store a secret on-device, so suppress the deprecation until a stable successor ships.
@file:Suppress("DEPRECATION")

package com.eventsnap.android.core.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.eventsnap.android.core.model.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "eventsnap_secure_settings"
private const val KEY_GROQ = "groq_api_key"
private const val KEY_CALENDAR = "default_calendar_id"
private const val KEY_REMINDER = "default_reminder_minutes"
private const val KEY_THEME = "theme_preference"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val DEFAULT_REMINDER_MINUTES = 30

/**
 * EncryptedSharedPreferences-backed settings. The Groq key never touches plaintext storage.
 * Reads are exposed as StateFlows seeded from the current persisted values so the UI updates
 * immediately after a write without needing a DataStore round-trip.
 */
class EncryptedSettingsStore(
    context: Context,
) : SettingsStore {
    private val prefs: SharedPreferences =
        run {
            val masterKey =
                MasterKey
                    .Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }

    private val _groqApiKey = MutableStateFlow(prefs.getString(KEY_GROQ, null))
    override val groqApiKey = _groqApiKey.asStateFlow()

    private val _defaultCalendarId =
        MutableStateFlow(
            prefs.getLong(KEY_CALENDAR, -1L).takeIf { it >= 0L },
        )
    override val defaultCalendarId = _defaultCalendarId.asStateFlow()

    private val _defaultReminderMinutes = MutableStateFlow(prefs.getInt(KEY_REMINDER, DEFAULT_REMINDER_MINUTES))
    override val defaultReminderMinutes = _defaultReminderMinutes.asStateFlow()

    private val _themePreference =
        MutableStateFlow(
            prefs
                .getString(KEY_THEME, null)
                ?.let { name -> runCatching { ThemePreference.valueOf(name) }.getOrNull() }
                ?: ThemePreference.SYSTEM,
        )
    override val themePreference = _themePreference.asStateFlow()

    // Dynamic color (Material You) defaults on; only meaningful on Android 12+.
    private val _dynamicColor = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    override val dynamicColor = _dynamicColor.asStateFlow()

    override suspend fun setGroqApiKey(key: String) {
        prefs.edit().putString(KEY_GROQ, key).apply()
        _groqApiKey.value = key
    }

    override suspend fun setDefaultCalendarId(id: Long) {
        prefs.edit().putLong(KEY_CALENDAR, id).apply()
        _defaultCalendarId.value = id
    }

    override suspend fun setDefaultReminderMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_REMINDER, minutes).apply()
        _defaultReminderMinutes.value = minutes
    }

    override suspend fun setThemePreference(preference: ThemePreference) {
        prefs.edit().putString(KEY_THEME, preference.name).apply()
        _themePreference.value = preference
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _dynamicColor.value = enabled
    }
}
