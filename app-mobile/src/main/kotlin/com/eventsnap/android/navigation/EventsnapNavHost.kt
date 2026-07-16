package com.eventsnap.android.navigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.history.EventHistoryDao
import com.eventsnap.android.core.model.AddedBatch
import com.eventsnap.android.feature.capture.CaptureRoute
import com.eventsnap.android.feature.capture.captureEntry
import com.eventsnap.android.feature.history.HistoryRoute
import com.eventsnap.android.feature.history.historyEntry
import com.eventsnap.android.feature.review.ReviewRoute
import com.eventsnap.android.feature.review.reviewEntry
import com.eventsnap.android.feature.settings.SettingsRoute
import com.eventsnap.android.feature.settings.settingsEntry
import org.koin.compose.koinInject

@Composable
fun EventsnapNavHost(
    modifier: Modifier = Modifier,
    sharedText: String? = null,
    sharedMediaUri: android.net.Uri? = null,
    launchAction: String? = null,
    onLaunchActionConsumed: () -> Unit = {},
) {
    val backStack: SnapshotStateList<NavKey> = remember { mutableStateListOf(CaptureRoute) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    // The just-added batch, awaiting its "Added · Undo" snackbar. Set when Review reports a save.
    var pendingUndo by remember { mutableStateOf<PendingUndo?>(null) }
    // Set when the up-front calendar permission request is denied, to trigger the recovery snackbar.
    var calendarPermissionDenied by remember { mutableStateOf(false) }
    val calendarWriter = koinInject<CalendarWriter>()
    val historyDao = koinInject<EventHistoryDao>()

    // App-level confirmation: shown after Review has already popped, so it never blocks navigation.
    LaunchedEffect(pendingUndo) {
        val undo = pendingUndo ?: return@LaunchedEffect
        val label = if (undo.count == 1) "Event added" else "${undo.count} events added"
        val result =
            snackbarHostState.showSnackbar(
                message = label,
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        if (result == SnackbarResult.ActionPerformed) {
            calendarWriter.deleteEvents(undo.batch.calendarEventIds)
            historyDao.deleteByIds(undo.batch.historyIds)
        }
        pendingUndo = null
    }

    // Request calendar permissions up-front so the review step can read/write calendars.
    // If the user denies, the calendar features are silently broken, so surface a recovery
    // snackbar that deep-links to the app's settings page where the grant can be flipped on.
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            val calendarGranted =
                grants[Manifest.permission.READ_CALENDAR] == true &&
                    grants[Manifest.permission.WRITE_CALENDAR] == true
            if (!calendarGranted) calendarPermissionDenied = true
        }

    LaunchedEffect(Unit) {
        val needed =
            listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                .filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
    }

    // Denied-calendar recovery: offer a one-tap jump to the app's system settings.
    LaunchedEffect(calendarPermissionDenied) {
        if (!calendarPermissionDenied) return@LaunchedEffect
        val result =
            snackbarHostState.showSnackbar(
                message = "Calendar access is off — EventSnap can't add events without it.",
                actionLabel = "Settings",
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
        if (result == SnackbarResult.ActionPerformed) {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData("package:${context.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
        }
        calendarPermissionDenied = false
    }

    val currentTabRoute =
        backStack.lastOrNull()?.takeIf {
            it == CaptureRoute || it == HistoryRoute || it == SettingsRoute
        }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        onLaunchActionConsumed = onLaunchActionConsumed,
                    )
                    reviewEntry(
                        onDone = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                        onEventsAdded = { count, batch ->
                            // Pop back to Capture immediately, then let the app-level snackbar offer Undo.
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                            pendingUndo = PendingUndo(count, batch)
                        },
                    )
                    historyEntry()
                    settingsEntry()
                },
        )
    }
}

/** A just-added batch awaiting its "Added · Undo" snackbar at the app level. */
private data class PendingUndo(
    val count: Int,
    val batch: AddedBatch,
)
