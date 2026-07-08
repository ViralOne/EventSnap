package com.eventsnap.android.feature.history.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eventsnap.android.feature.history.HistoryViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    HistoryScreenContent(modifier = modifier, state = state)
}
