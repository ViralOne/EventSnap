package com.eventsnap.android.feature.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.settings.components.SettingsScreen

fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsRoute> {
        SettingsScreen()
    }
}
