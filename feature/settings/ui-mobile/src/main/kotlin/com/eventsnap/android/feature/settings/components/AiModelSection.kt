package com.eventsnap.android.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm
import com.eventsnap.android.feature.settings.mvi.SettingsAction
import com.eventsnap.android.feature.settings.mvi.SettingsState

private val PROGRESS_SIZE = 18.dp

/**
 * Model pickers fed by Groq's live `GET /models` list, so the app isn't stuck on ids baked in at
 * release time. "Automatic" is the default and the recommended choice: it re-picks from the live
 * list on every capture, which is what survives a model being retired mid-release.
 */
@Composable
fun AiModelSection(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("AI models", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            if (state.isLoadingModels) {
                CircularProgressIndicator(modifier = Modifier.size(PROGRESS_SIZE))
            } else {
                TextButton(
                    onClick = { onAction(SettingsAction.RefreshModels) },
                    modifier = Modifier.testTag("settings_refresh_models"),
                ) {
                    Text("Refresh")
                }
            }
        }

        if (!state.hasSavedKey) {
            Text(
                "Save an API key to load the models Groq is serving.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }

        ModelPicker(
            label = "Text descriptions",
            arm = AiModelArm.TEXT,
            pinned = state.pinnedTextModel,
            resolved = state.resolvedTextModel,
            models = state.availableModels,
            onAction = onAction,
        )
        ModelPicker(
            label = "Photos & PDFs",
            arm = AiModelArm.VISION,
            pinned = state.pinnedVisionModel,
            resolved = state.resolvedVisionModel,
            // Reading an image needs a multimodal model; the guessed-capable ones sort first.
            models = state.availableModels,
            onAction = onAction,
            hint = "Needs a model that accepts images — those are marked \"vision\".",
        )
    }
}

@Composable
private fun ModelPicker(
    label: String,
    arm: AiModelArm,
    pinned: String?,
    resolved: String?,
    models: List<AiModel>,
    onAction: (SettingsAction) -> Unit,
    hint: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().testTag("settings_model_${arm.name}"),
        ) {
            Text(pinned ?: automaticLabel(resolved))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(automaticLabel(resolved)) },
                onClick = {
                    expanded = false
                    onAction(SettingsAction.ModelSelected(arm, null))
                },
            )
            models.forEach { model ->
                DropdownMenuItem(
                    text = { Text(modelLabel(model)) },
                    onClick = {
                        expanded = false
                        onAction(SettingsAction.ModelSelected(arm, model.id))
                    },
                )
            }
        }
        if (models.isEmpty()) {
            Text(
                "Model list unavailable — using built-in defaults.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (hint != null) {
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun automaticLabel(resolved: String?): String = if (resolved == null) "Automatic" else "Automatic ($resolved)"

private fun modelLabel(model: AiModel): String = if (model.supportsVision) "${model.id} · vision" else model.id
