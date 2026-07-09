package com.eventsnap.android.feature.history.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.ui.mobile.calendar.CalendarEventOpener
import com.eventsnap.android.feature.history.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    fun open(
        eventId: Long,
        edit: Boolean,
    ) {
        val ok = if (edit) CalendarEventOpener.edit(context, eventId) else CalendarEventOpener.view(context, eventId)
        if (!ok) {
            Toast.makeText(context, "Couldn't open the event in a calendar app.", Toast.LENGTH_SHORT).show()
        }
    }

    HistoryScreenContent(
        modifier = modifier,
        state = state,
        onOpenEvent = { open(it, edit = false) },
        onEditEvent = { open(it, edit = true) },
    )
}
