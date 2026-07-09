package com.eventsnap.android.feature.review.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.ui.mobile.HandleEffects
import com.eventsnap.android.feature.review.ReviewViewModel
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReviewScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReviewViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    // Set when events are saved; drives the "added · Undo" snackbar in a LaunchedEffect below.
    var saved by remember { mutableStateOf<ReviewEffect.ShowSaved?>(null) }

    // Reload fresh on every entry — the ViewModel can be reused across visits, so this clears
    // any events/selection left from a previous review and re-reads the newly captured batch.
    LaunchedEffect(Unit) { viewModel.setAction(ReviewAction.Load) }

    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            is ReviewEffect.ShowSaved -> saved = effect
            is ReviewEffect.NavigateBackToCapture -> onDone()
        }
    }

    // Show the confirmation snackbar with Undo, then navigate back once it resolves.
    LaunchedEffect(saved) {
        val current = saved ?: return@LaunchedEffect
        val label = if (current.count == 1) "Event added" else "${current.count} events added"
        val result =
            snackbarHostState.showSnackbar(
                message = label,
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.setAction(ReviewAction.Undo(current.batch))
        }
        saved = null
        onDone()
    }

    ReviewScreenContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::setAction,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}
