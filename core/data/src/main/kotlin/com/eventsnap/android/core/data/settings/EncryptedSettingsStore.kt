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
import timber.log.Timber

private const val PREFS_NAME = "eventsnap_secure_settings"
private const val KEY_GROQ = "groq_api_key"
private const val KEY_CALENDAR = "default_calendar_id"
private const val KEY_REMINDER = "default_reminder_minutes"
private const val KEY_THEME = "theme_preference"
private const val KEY_DYNAMIC_COLOR = "dynamic_color"
private const val KEY_TEXT_MODEL = "text_model"
private const val KEY_VISION_MODEL = "vision_model"
private const val DEFAULT_REMINDER_MINUTES = 30

/**
 * EncryptedSharedPreferences-backed settings. The Groq key never touches plaintext storage.
 * Reads are exposed as StateFlows seeded from the current persisted values so the UI updates
 * immediately after a write without needing a DataStore round-trip.
 */
class EncryptedSettingsStore(
    context: Context,
) : SettingsStore {
    private val prefs: SharedPreferences = openPrefs(context)

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

    // Null (the default) means automatic: GroqModelRegistry picks from Groq's live model list.
    private val _textModel = MutableStateFlow(prefs.getString(KEY_TEXT_MODEL, null))
    override val textModel = _textModel.asStateFlow()

    private val _visionModel = MutableStateFlow(prefs.getString(KEY_VISION_MODEL, null))
    override val visionModel = _visionModel.asStateFlow()

    override suspend fun setGroqApiKey(key: String) {
        prefs.edit().putString(KEY_GROQ, key).apply()
        _groqApiKey.value = key
    }

    override suspend fun setTextModel(modelId: String?) {
        putModel(KEY_TEXT_MODEL, modelId, _textModel)
    }

    override suspend fun setVisionModel(modelId: String?) {
        putModel(KEY_VISION_MODEL, modelId, _visionModel)
    }

    /** Stores a pinned model id, removing the key entirely for null/blank (back to automatic). */
    private fun putModel(
        key: String,
        modelId: String?,
        flow: MutableStateFlow<String?>,
    ) {
        val cleaned = modelId?.trim()?.takeIf { it.isNotEmpty() }
        prefs.edit().apply { if (cleaned == null) remove(key) else putString(key, cleaned) }.apply()
        flow.value = cleaned
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

private fun openPrefs(context: Context): SharedPreferences =
    openEncryptedPrefs(
        create = { createPrefs(context) },
        discardUnreadable = { context.deleteSharedPreferences(PREFS_NAME) },
    )

/**
 * Opens encrypted prefs, recovering from a file the device can no longer decrypt: on failure it
 * discards the file and retries [create] once.
 *
 * The keystore MasterKey lives outside the app's data directory and does not travel with it, so the
 * two can get out of sync — an uninstall/reinstall where auto-backup restores the encrypted file but
 * not the key, a restore onto another device, a keystore reset. EncryptedSharedPreferences then
 * throws AEADBadTagException on open, and because this store is built during Koin startup that took
 * the whole app down before any UI existed: the app simply would not launch.
 *
 * The file holds only settings (an API key the user can re-paste, plus preferences), so starting
 * from empty settings is strictly better than not starting. A second failure is real breakage and
 * propagates.
 *
 * Catching Exception is deliberate: Tink surfaces an undecryptable keyset as a checked
 * GeneralSecurityException/IOException in some paths and as an unchecked wrapper in others, and
 * every one of them must degrade to empty settings rather than a crash-on-launch.
 *
 * Public rather than private so it can be tested without a device keystore.
 */
@Suppress("TooGenericExceptionCaught")
fun openEncryptedPrefs(
    create: () -> SharedPreferences,
    discardUnreadable: () -> Unit,
): SharedPreferences =
    try {
        create()
    } catch (error: Exception) {
        Timber.w(error, "Encrypted settings could not be decrypted — starting from empty settings")
        discardUnreadable()
        create()
    }

private fun createPrefs(context: Context): SharedPreferences {
    val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    return EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}
