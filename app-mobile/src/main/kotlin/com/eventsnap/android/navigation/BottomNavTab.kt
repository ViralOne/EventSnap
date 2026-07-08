package com.eventsnap.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.eventsnap.android.feature.capture.CaptureRoute
import com.eventsnap.android.feature.history.HistoryRoute
import com.eventsnap.android.feature.settings.SettingsRoute

enum class BottomNavTab(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
) {
    Capture(CaptureRoute, "Capture", Icons.Filled.Add),
    History(HistoryRoute, "History", Icons.Filled.History),
    Settings(SettingsRoute, "Settings", Icons.Filled.Settings),
}
