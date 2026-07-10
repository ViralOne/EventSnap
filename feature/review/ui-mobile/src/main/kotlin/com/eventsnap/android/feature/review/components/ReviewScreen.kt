package com.eventsnap.android.feature.review.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.model.AddedBatch
import com.eventsnap.android.core.ui.mobile.HandleEffects
import com.eventsnap.android.feature.review.ReviewViewModel
import com.eventsnap.android.feature.review.mvi.ReviewAction
import com.eventsnap.android.feature.review.mvi.ReviewEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReviewScreen(
    onDone: () -> Unit,
    onEventsAdded: (count: Int, batch: AddedBatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReviewViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Request coarse location once per composition to bias place-search suggestions. The result is
    // never read here — the provider checks the grant synchronously on each search. If denied, the
    // search simply runs without bias (no UX impact, no crash).
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }
    LaunchedEffect(Unit) { locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) }

    // Reload fresh on every entry — the ViewModel can be reused across visits, so this clears
    // any events/selection left from a previous review and re-reads the newly captured batch.
    LaunchedEffect(Unit) { viewModel.setAction(ReviewAction.Load) }

    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            // Hand the batch to the app level, which pops back instantly and shows the "Added · Undo"
            // snackbar there — so this screen doesn't block for the snackbar's full duration.
            is ReviewEffect.ShowSaved -> onEventsAdded(effect.count, effect.batch)
            is ReviewEffect.NavigateBackToCapture -> onDone()
        }
    }

    ReviewScreenContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::setAction,
    )
}
