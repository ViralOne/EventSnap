package com.eventsnap.android.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.eventsnap.android.feature.capture.CaptureRoute
import com.eventsnap.android.feature.capture.captureEntry
import com.eventsnap.android.feature.history.HistoryRoute
import com.eventsnap.android.feature.history.historyEntry
import com.eventsnap.android.feature.review.ReviewRoute
import com.eventsnap.android.feature.review.reviewEntry
import com.eventsnap.android.feature.settings.SettingsRoute
import com.eventsnap.android.feature.settings.settingsEntry

@Composable
fun EventsnapNavHost(
    modifier: Modifier = Modifier,
    sharedText: String? = null,
    sharedMediaUri: android.net.Uri? = null,
    launchAction: String? = null,
) {
    val backStack: SnapshotStateList<NavKey> = remember { mutableStateListOf(CaptureRoute) }
    val context = LocalContext.current

    // Request calendar permissions up-front so the review step can read/write calendars.
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { /* result handled implicitly — features degrade to an error if denied */ }

    LaunchedEffect(Unit) {
        val needed =
            listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                .filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    val currentTabRoute =
        backStack.lastOrNull()?.takeIf {
            it == CaptureRoute || it == HistoryRoute || it == SettingsRoute
        }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            EventsnapBottomNav(
                currentRoute = currentTabRoute,
                onTabSelected = { tab ->
                    // Reset to the tab root; tabs are top-level destinations.
                    backStack.clear()
                    backStack.add(tab.route)
                },
            )
        },
    ) { innerPadding ->
        NavDisplay(
            // consumeWindowInsets tells descendants (e.g. the capture input bar's
            // windowInsetsPadding) that the bottom-nav / system-nav insets are already
            // applied here — without it they double-count and leave a dark band above the keyboard.
            modifier =
                Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            backStack = backStack,
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryProvider =
                entryProvider {
                    captureEntry(
                        onNavigateToReview = { backStack.add(ReviewRoute) },
                        onNavigateToSettings = {
                            backStack.clear()
                            backStack.add(SettingsRoute)
                        },
                        sharedText = sharedText,
                        sharedMediaUri = sharedMediaUri,
                        launchAction = launchAction,
                    )
                    reviewEntry(
                        onDone = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                    )
                    historyEntry()
                    settingsEntry()
                },
        )
    }
}
