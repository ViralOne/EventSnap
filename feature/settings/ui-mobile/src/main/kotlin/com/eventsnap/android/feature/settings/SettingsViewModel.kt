package com.eventsnap.android.feature.settings

import androidx.lifecycle.viewModelScope
import com.eventsnap.android.core.BaseViewModel
import com.eventsnap.android.core.model.AiModelArm
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
            val pinnedText = repository.textModel.first()
            val pinnedVision = repository.visionModel.first()
            setState {
                copy(
                    hasSavedKey = !key.isNullOrBlank(),
                    reminderMinutes = reminder,
                    defaultCalendarId = defaultCal,
                    themePreference = theme,
                    dynamicColor = dynamic,
                    pinnedTextModel = pinnedText,
                    pinnedVisionModel = pinnedVision,
                )
            }
            runCatching { repository.writableCalendars() }
                .onSuccess { calendars -> setState { copy(calendars = calendars.toImmutableList()) } }
            loadModels(forceRefresh = false)
        }
    }

    override suspend fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.ApiKeyChanged -> {
                setState { copy(apiKeyInput = action.value) }
            }

            is SettingsAction.SaveApiKey -> {
                saveKey()
            }

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

            is SettingsAction.ModelSelected -> {
                selectModel(action.arm, action.modelId)
            }

            is SettingsAction.RefreshModels -> {
                loadModels(forceRefresh = true)
            }

            is SettingsAction.MessageDismissed -> {
                setState { copy(savedMessage = null) }
            }
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
        // The model list needs a key, so the first fetch usually only becomes possible right here.
        loadModels(forceRefresh = true)
    }

    private suspend fun selectModel(
        arm: AiModelArm,
        modelId: String?,
    ) {
        repository.setModel(arm, modelId)
        setState {
            when (arm) {
                AiModelArm.TEXT -> copy(pinnedTextModel = modelId)
                AiModelArm.VISION -> copy(pinnedVisionModel = modelId)
            }
        }
        setState { copy(savedMessage = if (modelId == null) "Back to automatic model choice" else "Model set to $modelId") }
        refreshResolvedModels()
    }

    /** Pulls Groq's live model list; failures leave the previous list in place rather than erroring. */
    private suspend fun loadModels(forceRefresh: Boolean) {
        setState { copy(isLoadingModels = true) }
        val models = runCatching { repository.availableModels(forceRefresh) }.getOrNull().orEmpty()
        // Only a refresh the user asked for reports failure; the silent startup load stays quiet.
        val failedRefresh = forceRefresh && models.isEmpty()
        setState {
            copy(
                isLoadingModels = false,
                availableModels = models.toImmutableList(),
                savedMessage = if (failedRefresh) "Could not load models — check your key and connection." else savedMessage,
            )
        }
        refreshResolvedModels()
    }

    /** Shows what "Automatic" actually resolves to, so the choice isn't invisible. */
    private suspend fun refreshResolvedModels() {
        val text = runCatching { repository.resolvedModel(AiModelArm.TEXT) }.getOrNull()
        val vision = runCatching { repository.resolvedModel(AiModelArm.VISION) }.getOrNull()
        setState { copy(resolvedTextModel = text, resolvedVisionModel = vision) }
    }
}
