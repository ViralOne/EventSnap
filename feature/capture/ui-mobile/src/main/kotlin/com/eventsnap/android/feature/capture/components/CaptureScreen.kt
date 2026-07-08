package com.eventsnap.android.feature.capture.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.model.CaptureInput
import com.eventsnap.android.core.ui.mobile.HandleEffects
import com.eventsnap.android.feature.capture.CaptureViewModel
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun CaptureScreen(
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier,
    sharedText: String? = null,
) {
    val viewModel: CaptureViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            viewModel.setAction(CaptureAction.SubmitSharedText(sharedText))
        }
    }

    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            is CaptureEffect.NavigateToReview -> onNavigateToReview()
        }
    }

    val imagePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                if (bytes != null) {
                    viewModel.setAction(CaptureAction.SubmitImage(CaptureInput.Image(bytes = bytes, mimeType = mime)))
                }
            }
        }

    CaptureScreenContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::setAction,
        onPickImage = {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
    )
}
