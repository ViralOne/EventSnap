package com.eventsnap.android.core.ui.mobile.dev

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Wraps the app content. When [enabled] is false this is a zero-cost passthrough — prod builds
 * pay nothing. When true, mounts the shake listener + dev-tools broadcast receiver and shows
 * [EnvSelectorDialog] on either trigger.
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
    ShakeListener(onShake = { showDialog = true })
    DevToolsBroadcastListener(onTrigger = { showDialog = true })
    content()
    if (showDialog) {
        EnvSelectorDialog(onDismiss = { showDialog = false })
    }
}
