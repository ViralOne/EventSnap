package com.eventsnap.android.feature.capture

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.capture.components.CaptureScreen

fun EntryProviderScope<NavKey>.captureEntry(
    onNavigateToReview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    sharedText: String? = null,
    sharedMediaUri: android.net.Uri? = null,
    launchAction: String? = null,
) {
    entry<CaptureRoute> {
        CaptureScreen(
            onNavigateToReview = onNavigateToReview,
            onNavigateToSettings = onNavigateToSettings,
            sharedText = sharedText,
            sharedMediaUri = sharedMediaUri,
            launchAction = launchAction,
        )
    }
}
