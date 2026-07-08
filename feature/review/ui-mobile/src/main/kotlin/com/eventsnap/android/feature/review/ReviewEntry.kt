package com.eventsnap.android.feature.review

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.review.components.ReviewScreen

fun EntryProviderScope<NavKey>.reviewEntry(onDone: () -> Unit) {
    entry<ReviewRoute> {
        ReviewScreen(onDone = onDone)
    }
}
