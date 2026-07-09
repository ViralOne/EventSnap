package com.eventsnap.android.feature.capture.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.model.CaptureInput
import com.eventsnap.android.core.ui.mobile.HandleEffects
import com.eventsnap.android.core.ui.mobile.media.MediaReaders
import com.eventsnap.android.feature.capture.CaptureViewModel
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureEffect
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun CaptureScreen(
    onNavigateToReview: () -> Unit,
    modifier: Modifier = Modifier,
    sharedText: String? = null,
    sharedMediaUri: Uri? = null,
) {
    val viewModel: CaptureViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun submitUri(uri: Uri?) {
        if (uri == null) return // user cancelled the picker — not an error
        val bytes = MediaReaders.readAsJpeg(context, uri)
        if (bytes == null) {
            viewModel.setAction(
                CaptureAction.MediaError("That file looks empty or unreadable. Pick a valid image or PDF."),
            )
            return
        }
        viewModel.setAction(CaptureAction.SubmitImage(CaptureInput.Image(bytes = bytes, mimeType = "image/jpeg")))
    }

    LaunchedEffect(sharedText) {
        if (!sharedText.isNullOrBlank()) {
            viewModel.setAction(CaptureAction.SubmitSharedText(sharedText))
        }
    }

    // A screenshot/photo/PDF shared into the app: read and extract it just like a picked file.
    LaunchedEffect(sharedMediaUri) {
        if (sharedMediaUri != null) submitUri(sharedMediaUri)
    }

    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            is CaptureEffect.NavigateToReview -> onNavigateToReview()
        }
    }

    // Gallery (photo picker — no storage permission needed on API 29+).
    val galleryPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri -> submitUri(uri) }

    // Files (image or PDF).
    val filePicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri -> submitUri(uri) }

    // Camera — writes to a temp file exposed via FileProvider, then we read it back.
    val cameraImageUri =
        remember {
            val dir = File(context.cacheDir, "captures").apply { mkdirs() }
            val file = File(dir, "capture.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success -> if (success) submitUri(cameraImageUri) }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> if (granted) cameraLauncher.launch(cameraImageUri) }

    // Voice — system speech recognizer returns a transcript we drop into the description.
    val voiceLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val spoken =
                result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
            if (!spoken.isNullOrBlank()) viewModel.setAction(CaptureAction.DescriptionChanged(spoken))
        }

    CaptureScreenContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::setAction,
        onPickFromGallery = {
            galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onTakePhoto = {
            val granted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
            if (granted) cameraLauncher.launch(cameraImageUri) else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        },
        onPickFromFiles = {
            filePicker.launch(arrayOf("image/*", "application/pdf"))
        },
        onStartVoice = { voiceLauncher.launch(buildVoiceIntent()) },
    )
}

private fun buildVoiceIntent(): android.content.Intent {
    // Follow the device language so speech is recognized in the user's own language
    // (e.g. Romanian). Without EXTRA_LANGUAGE the recognizer falls back to English.
    val locale = java.util.Locale.getDefault()
    val languageTag = locale.toLanguageTag()
    return android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
        putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your event")
    }
}
