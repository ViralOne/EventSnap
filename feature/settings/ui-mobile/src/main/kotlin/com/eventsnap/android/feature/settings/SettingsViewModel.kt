package com.eventsnap.android.feature.settings

import androidx.lifecycle.viewModelScope
import com.eventsnap.android.core.BaseViewModel
import com.eventsnap.android.feature.settings.data.SettingsRepository
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.feature.settings.mvi.SettingsEffect
import com.eventsnap.android.feature.settings.mvi.SettingsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
) : BaseViewModel<SettingsState, SettingsAction, SettingsEffect>(SettingsState()) {
    init {
        viewModelScope.launch {
            val key = repository.groqApiKey.first()
            val reminder = repository.defaultReminderMinutes.first()
            val defaultCal = repository.defaultCalendarId.first()
            val theme = repository.themePreference.first()
            val dynamic = repository.dynamicColor.first()
            setState {
                copy(
                    hasSavedKey = !key.isNullOrBlank(),
                    reminderMinutes = reminder,
                    defaultCalendarId = defaultCal,
                    themePreference = theme,
                    dynamicColor = dynamic,
                )
            }
            runCatching { repository.writableCalendars() }
                .onSuccess { calendars -> setState { copy(calendars = calendars.toImmutableList()) } }
        }
    }

    override suspend fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ApiKeyChanged -> setState { copy(apiKeyInput = action.value) }
            is SettingsAction.SaveApiKey -> saveKey()
            is SettingsAction.DefaultCalendarSelected -> {
                repository.setDefaultCalendarId(action.id)
                setState { copy(defaultCalendarId = action.id, savedMessage = "Default calendar saved") }
            }
            is SettingsAction.ReminderChanged -> {
                repository.setDefaultReminderMinutes(action.minutes)
                setState { copy(reminderMinutes = action.minutes) }
            }
            is SettingsAction.ThemeSelected -> {
                repository.setThemePreference(action.preference)
                setState { copy(themePreference = action.preference) }
            }
            is SettingsAction.DynamicColorToggled -> {
                repository.setDynamicColor(action.enabled)
                setState { copy(dynamicColor = action.enabled) }
            }
            is SettingsAction.MessageDismissed -> setState { copy(savedMessage = null) }
        }
    }

    private suspend fun saveKey() {
        val key = state.value.apiKeyInput.trim()
        if (key.isBlank()) {
            setState { copy(savedMessage = "Enter a key first.") }
            return
        }
        repository.setGroqApiKey(key)
        setState { copy(apiKeyInput = "", hasSavedKey = true, savedMessage = "API key saved") }
    }
}
