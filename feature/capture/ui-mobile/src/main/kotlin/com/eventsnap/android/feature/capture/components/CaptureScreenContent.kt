package com.eventsnap.android.feature.capture.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.eventsnap.android.core.designsystem.theme.EventsnapPreviews
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.designsystem.theme.Spacing
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureState

@Composable
fun CaptureScreenContent(
    state: CaptureState,
    onAction: (CaptureAction) -> Unit,
    onPickImage: () -> Unit,
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
        Text(
            text = "Describe an event or add a photo",
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedTextField(
            value = state.description,
            onValueChange = { onAction(CaptureAction.DescriptionChanged(it)) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("capture_description_field"),
            label = { Text("e.g. Dinner with Ana Fri 8pm at Osteria, remind 1h before") },
            enabled = !state.isProcessing,
            minLines = 3,
        )

        Button(
            onClick = { onAction(CaptureAction.SubmitText) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("capture_submit_text"),
            enabled = !state.isProcessing,
        ) {
            Text("Extract events")
        }

        OutlinedButton(
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isProcessing,
        ) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Text("  Pick an image", modifier = Modifier.padding(start = Spacing.xs))
        }

        if (state.isProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(top = Spacing.md))
            Text("Reading with AI…", style = MaterialTheme.typography.bodyMedium)
        }

        val error = state.error
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag("capture_error"),
            )
        }
    }
}

@EventsnapPreviews
@Composable
private fun CaptureScreenContentPreview() {
    EventsnapTheme {
        CaptureScreenContent(
            state = CaptureState(description = "Team standup tomorrow 9am"),
            onAction = {},
            onPickImage = {},
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
            onPickImage = {},
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
            onPickImage = {},
        )
    }
}
