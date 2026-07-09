package com.eventsnap.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.designsystem.theme.EventsnapTheme
import com.eventsnap.android.core.model.ThemePreference
import com.eventsnap.android.core.ui.mobile.dev.DevToolsHost
import com.eventsnap.android.navigation.EventsnapNavHost
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val settingsStore: SettingsStore by inject()
    private var sharedText by mutableStateOf<String?>(null)
    private var sharedMediaUri by mutableStateOf<Uri?>(null)
    private var launchAction by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consumeShareIntent(intent)
        launchAction = intent?.getStringExtra(EXTRA_LAUNCH_ACTION)
        setContent {
            val themePreference by settingsStore.themePreference.collectAsStateWithLifecycle(ThemePreference.SYSTEM)
            val dynamicColor by settingsStore.dynamicColor.collectAsStateWithLifecycle(true)
            val darkTheme =
                when (themePreference) {
                    ThemePreference.SYSTEM -> isSystemInDarkTheme()
                    ThemePreference.LIGHT -> false
                    ThemePreference.DARK -> true
                }
            EventsnapTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
                DevToolsHost(enabled = BuildConfig.IS_QA) {
                    EventsnapNavHost(
                        sharedText = sharedText,
                        sharedMediaUri = sharedMediaUri,
                        launchAction = launchAction,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        consumeShareIntent(intent)
        launchAction = intent.getStringExtra(EXTRA_LAUNCH_ACTION)
    }

    /** Reads whatever was shared into the app (text, or an image/PDF Uri) from an ACTION_SEND intent. */
    private fun consumeShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        sharedMediaUri = extractSharedStream(intent)
    }

    @Suppress("DEPRECATION")
    private fun extractSharedStream(intent: Intent): Uri? {
        val type = intent.type.orEmpty()
        if (!type.startsWith("image/") && type != "application/pdf") return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    companion object {
        /** Extra used by home-screen shortcuts, the widget, and the QS tile to auto-start a capture. */
        const val EXTRA_LAUNCH_ACTION = "com.eventsnap.android.LAUNCH_ACTION"
    }
}
