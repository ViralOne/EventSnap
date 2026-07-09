package com.eventsnap.android.feature.capture.components

/**
 * The system-launcher callbacks passed from [CaptureScreen] into [CaptureScreenContent].
 * Bundled into one holder so the content stays close to the pure (state, onAction, modifier) shape;
 * these launchers are the one allowed exception (they need Activity result APIs from the Screen).
 */
data class CaptureLaunchers(
    val onPickFromGallery: () -> Unit,
    val onTakePhoto: () -> Unit,
    val onPickFromFiles: () -> Unit,
    val onStartVoice: () -> Unit,
)
