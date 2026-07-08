package com.eventsnap.android.feature.review.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.core.ui.mobile.HandleEffects
import com.eventsnap.android.feature.review.ReviewViewModel
import com.eventsnap.android.feature.review.mvi.ReviewEffect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReviewScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReviewViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    HandleEffects(viewModel.effects) { effect ->
        when (effect) {
            is ReviewEffect.ShowSaved ->
                Toast.makeText(context, "Added ${effect.count} event(s)", Toast.LENGTH_SHORT).show()
            is ReviewEffect.NavigateBackToCapture -> onDone()
        }
    }

    ReviewScreenContent(
        modifier = modifier,
        state = state,
        onAction = viewModel::setAction,
    )
}
