package com.eventsnap.android.feature.settings.mvi

import com.eventsnap.android.core.ViewAction
import com.eventsnap.android.core.ViewSideEffect
import com.eventsnap.android.core.ViewState
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SettingsState(
    val apiKeyInput: String = "",
    val hasSavedKey: Boolean = false,
    val calendars: ImmutableList<TargetCalendar> = persistentListOf(),
    val defaultCalendarId: Long? = null,
    val reminderMinutes: Int = 30,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColor: Boolean = true,
    val savedMessage: String? = null,
) : ViewState

sealed interface SettingsAction : ViewAction {
    data class ApiKeyChanged(
        val value: String,
    ) : SettingsAction

    data object SaveApiKey : SettingsAction

    data class DefaultCalendarSelected(
        val id: Long,
    ) : SettingsAction

    data class ReminderChanged(
        val minutes: Int,
    ) : SettingsAction

    data class ThemeSelected(
        val preference: ThemePreference,
    ) : SettingsAction

    data class DynamicColorToggled(
        val enabled: Boolean,
    ) : SettingsAction

    data object MessageDismissed : SettingsAction
}

sealed interface SettingsEffect : ViewSideEffect
