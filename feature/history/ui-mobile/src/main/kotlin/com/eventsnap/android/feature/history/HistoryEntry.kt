package com.eventsnap.android.feature.history

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.history.components.HistoryScreen

fun EntryProviderScope<NavKey>.historyEntry() {
    entry<HistoryRoute> {
        HistoryScreen()
    }
}
