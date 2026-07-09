package com.eventsnap.android.core.ui.mobile.dev

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Wraps the app content. When [enabled] is false this is a zero-cost passthrough — prod builds
 * pay nothing. When true, mounts ONLY the dev-tools broadcast receiver (triggered via
 * `adb shell am broadcast`) and shows [EnvSelectorDialog].
 *
 * The shake-to-open trigger was intentionally removed: normal phone movement crossed the
 * accelerometer threshold and popped the API-environment dialog on real users. The broadcast
 * trigger cannot fire by accident, so it's the only debug entry point now.
 */
@Composable
fun DevToolsHost(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }
    var showDialog by remember { mutableStateOf(false) }
    DevToolsBroadcastListener(onTrigger = { showDialog = true })
    content()
    if (showDialog) {
        EnvSelectorDialog(onDismiss = { showDialog = false })
    }
}
