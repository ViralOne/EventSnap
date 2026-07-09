package com.eventsnap.android.feature.capture

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.capture.components.CaptureScreen

fun EntryProviderScope<NavKey>.captureEntry(
    onNavigateToReview: () -> Unit,
    sharedText: String? = null,
    sharedMediaUri: android.net.Uri? = null,
) {
    entry<CaptureRoute> {
        CaptureScreen(
            onNavigateToReview = onNavigateToReview,
            sharedText = sharedText,
            sharedMediaUri = sharedMediaUri,
        )
    }
}
