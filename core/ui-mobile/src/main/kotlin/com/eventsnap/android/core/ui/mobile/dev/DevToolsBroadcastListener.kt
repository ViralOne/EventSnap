package com.eventsnap.android.core.ui.mobile.dev

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Listens for the dev-tools broadcast (`<applicationId>.OPEN_DEV_TOOLS`) and invokes [onTrigger].
 * The action is namespaced with the app's packageName so receivers across apps don't collide.
 */
@Composable
fun DevToolsBroadcastListener(onTrigger: () -> Unit) {
    val context = LocalContext.current
    val latestOnTrigger by rememberUpdatedState(onTrigger)

    DisposableEffect(context) {
        val action = "${context.packageName}.OPEN_DEV_TOOLS"
        val receiver =
            object : BroadcastReceiver() {
                override fun onReceive(
                    c: Context?,
                    intent: Intent?,
                ) {
                    if (intent?.action == action) latestOnTrigger()
                }
            }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(action),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }
}
