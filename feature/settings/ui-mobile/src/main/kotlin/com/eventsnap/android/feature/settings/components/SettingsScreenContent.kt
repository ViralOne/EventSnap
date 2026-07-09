package com.eventsnap.android.feature.settings.components

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.core.model.TargetCalendar
import com.eventsnap.android.core.model.ThemePreference
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.feature.settings.mvi.SettingsState
import kotlinx.collections.immutable.persistentListOf

private val REMINDER_OPTIONS = listOf(0, 10, 30, 60, 1440)

private val THEME_OPTIONS =
    listOf(
        ThemePreference.SYSTEM to "System default",
        ThemePreference.LIGHT to "Light",
        ThemePreference.DARK to "Dark",
    )

@Composable
fun SettingsScreenContent(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge)

        Text("Groq API key", style = MaterialTheme.typography.titleMedium)
        Text(
            if (state.hasSavedKey) "A key is saved. Enter a new one to replace it." else "No key saved yet.",
            style = MaterialTheme.typography.bodySmall,
        )
        OutlinedTextField(
            value = state.apiKeyInput,
            onValueChange = { onAction(SettingsAction.ApiKeyChanged(it)) },
            label = { Text("Groq API key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().testTag("settings_api_key_field"),
        )
        Button(
            onClick = { onAction(SettingsAction.SaveApiKey) },
            modifier = Modifier.fillMaxWidth().testTag("settings_save_key"),
        ) {
            Text("Save key")
        }

        Text("Default reminder", style = MaterialTheme.typography.titleMedium)
        REMINDER_OPTIONS.forEach { minutes ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.reminderMinutes == minutes,
                            onClick = { onAction(SettingsAction.ReminderChanged(minutes)) },
                        ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = state.reminderMinutes == minutes,
                    onClick = { onAction(SettingsAction.ReminderChanged(minutes)) },
                )
                Text(reminderLabel(minutes), modifier = Modifier.padding(start = Spacing.sm))
            }
        }

        if (state.calendars.isNotEmpty()) {
            Text("Default calendar", style = MaterialTheme.typography.titleMedium)
            state.calendars.forEach { calendar ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = state.defaultCalendarId == calendar.id,
                                onClick = { onAction(SettingsAction.DefaultCalendarSelected(calendar.id)) },
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = state.defaultCalendarId == calendar.id,
                        onClick = { onAction(SettingsAction.DefaultCalendarSelected(calendar.id)) },
                    )
                    Text(
                        "${calendar.displayName} (${calendar.accountName})",
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }
        }

        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        THEME_OPTIONS.forEach { (preference, label) ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = state.themePreference == preference,
                            onClick = { onAction(SettingsAction.ThemeSelected(preference)) },
                        ).testTag("settings_theme_${preference.name}"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = state.themePreference == preference,
                    onClick = { onAction(SettingsAction.ThemeSelected(preference)) },
                )
                Text(label, modifier = Modifier.padding(start = Spacing.sm))
            }
        }

        // Material You dynamic color is only available on Android 12+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Dynamic color", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Use colors from your wallpaper",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.dynamicColor,
                    onCheckedChange = { onAction(SettingsAction.DynamicColorToggled(it)) },
                    modifier = Modifier.testTag("settings_dynamic_color"),
                )
            }
        }

        val message = state.savedMessage
        if (message != null) {
            Text(
                message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("settings_message"),
            )
        }
    }
}

private fun reminderLabel(minutes: Int): String =
    when (minutes) {
        0 -> "At time of event"
        60 -> "1 hour before"
        1440 -> "1 day before"
        else -> "$minutes minutes before"
    }

@EventsnapPreviews
@Composable
private fun SettingsScreenContentPreview() {
    EventsnapTheme {
        SettingsScreenContent(
            state =
                SettingsState(
                    hasSavedKey = true,
                    reminderMinutes = 30,
                    calendars =
                        persistentListOf(
                            TargetCalendar(id = 1, displayName = "Personal", accountName = "me@gmail.com", isPrimary = true),
                        ),
                    defaultCalendarId = 1,
                ),
            onAction = {},
        )
    }
}
