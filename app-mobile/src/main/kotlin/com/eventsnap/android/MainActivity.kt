package com.eventsnap.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.ui.mobile.dev.DevToolsHost
import com.eventsnap.android.navigation.EventsnapNavHost

class MainActivity : ComponentActivity() {
    private var sharedText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedText = extractSharedText(intent)
        setContent {
            EventsnapTheme {
                DevToolsHost(enabled = BuildConfig.IS_QA) {
                    EventsnapNavHost(sharedText = sharedText)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        sharedText = extractSharedText(intent)
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }
}
