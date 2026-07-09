package com.eventsnap.android.feature.capture.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureState

@Composable
fun CaptureScreenContent(
    state: CaptureState,
    onAction: (CaptureAction) -> Unit,
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFromFiles: () -> Unit,
    onStartVoice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Pad the whole layout by the UNION (max, not sum) of the keyboard and navigation-bar insets,
    // so the input bar docks right on the keyboard with no dark band below it.
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg),
        ) {
            Spacer(Modifier.height(Spacing.lg))

            EyebrowLabel()
            Spacer(Modifier.height(Spacing.md))

            Text(
                text = "From a photo\nto your calendar.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 44.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = "Snap it, speak it, or describe it — get an event on your calendar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.lg))
            HowItWorks()
            Spacer(Modifier.height(Spacing.lg))

            SectionLabel("GENERATE AN EVENT")
            Spacer(Modifier.height(Spacing.sm))

            ActionRow(
                icon = Icons.Filled.Image,
                title = "Select from gallery",
                subtitle = "From a screenshot or a photo",
                enabled = !state.isProcessing,
                onClick = onPickFromGallery,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ActionRow(
                icon = Icons.Filled.PhotoCamera,
                title = "Take a photo",
                subtitle = "Frame a poster or a ticket",
                enabled = !state.isProcessing,
                onClick = onTakePhoto,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ActionRow(
                icon = Icons.Filled.AttachFile,
                title = "Select from files",
                subtitle = "PDF or image",
                enabled = !state.isProcessing,
                onClick = onPickFromFiles,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ActionRow(
                icon = Icons.Filled.Mic,
                title = "Speak an event",
                subtitle = "Dictate with your voice",
                enabled = !state.isProcessing,
                onClick = onStartVoice,
            )

            if (state.isProcessing) {
                Spacer(Modifier.height(Spacing.lg))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(
                        "Reading with AI…",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }

            val error = state.error
            if (error != null) {
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().testTag("capture_error"),
                )
            }

            Spacer(Modifier.height(Spacing.lg))
        }

        DockedInputBar(
            value = state.description,
            enabled = !state.isProcessing,
            onValueChange = { onAction(CaptureAction.DescriptionChanged(it)) },
            onSubmit = { onAction(CaptureAction.SubmitText) },
            onVoice = onStartVoice,
        )
    }
}

@Composable
private fun EyebrowLabel() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
        Text(
            text = "INSTANTLY",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 2.sp,
    )
}

@Composable
private fun HowItWorks() {
    var expanded by remember { mutableStateOf(false) }
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "HOW DOES IT WORK?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(
                text =
                    "Pick a photo, snap a poster, attach a PDF, speak, or just type. " +
                        "The AI reads it, pulls out the date, time, place and title, and you " +
                        "confirm before it lands on your calendar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.md),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(vertical = Spacing.md)
                .testTag("action_$title"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DockedInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onVoice: () -> Unit,
) {
    Surface(tonalElevation = 3.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
            verticalAlignment = Alignment.Bottom,
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("capture_description_field"),
                placeholder = { Text("…or describe an event") },
                shape = RoundedCornerShape(28.dp),
                // Grows from 1 line up to 5 as the user types a longer description, then scrolls.
                minLines = 1,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                colors =
                    TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
            )
            Spacer(Modifier.size(Spacing.sm))
            val hasText = value.isNotBlank()
            Box(
                modifier =
                    Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(16.dp))
                        .clickable(enabled = enabled) { if (hasText) onSubmit() else onVoice() }
                        .testTag(if (hasText) "capture_send" else "capture_voice"),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (hasText) Icons.Filled.Send else Icons.Filled.Mic,
                    contentDescription = if (hasText) "Extract events" else "Speak",
                    tint = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}

@EventsnapPreviews
@Composable
private fun CaptureScreenContentPreview() {
    EventsnapTheme {
        CaptureScreenContent(
            state = CaptureState(),
            onAction = {},
            onPickFromGallery = {},
            onTakePhoto = {},
            onPickFromFiles = {},
            onStartVoice = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun CaptureScreenContentTypingPreview() {
    EventsnapTheme {
        CaptureScreenContent(
            state = CaptureState(description = "Dentist Tuesday 3pm"),
            onAction = {},
            onPickFromGallery = {},
            onTakePhoto = {},
            onPickFromFiles = {},
            onStartVoice = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun CaptureScreenContentProcessingPreview() {
    EventsnapTheme {
        CaptureScreenContent(
            state = CaptureState(description = "Concert Sat", isProcessing = true),
            onAction = {},
            onPickFromGallery = {},
            onTakePhoto = {},
            onPickFromFiles = {},
            onStartVoice = {},
        )
    }
}

@EventsnapPreviews
@Composable
private fun CaptureScreenContentErrorPreview() {
    EventsnapTheme {
        CaptureScreenContent(
            state = CaptureState(error = "No Groq API key set. Add one in Settings."),
            onAction = {},
            onPickFromGallery = {},
            onTakePhoto = {},
            onPickFromFiles = {},
            onStartVoice = {},
        )
    }
}
