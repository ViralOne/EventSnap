package com.eventsnap.android.feature.history

import androidx.lifecycle.viewModelScope
import com.eventsnap.android.core.BaseViewModel
import com.eventsnap.android.feature.history.data.HistoryRepository
import com.eventsnap.android.feature.history.mvi.HistoryAction
import com.eventsnap.android.feature.history.mvi.HistoryEffect
import com.eventsnap.android.feature.history.mvi.HistoryState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: HistoryRepository,
) : BaseViewModel<HistoryState, HistoryAction, HistoryEffect>(HistoryState()) {
    init {
        observe()
    }

    override suspend fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.Load -> observe()
            is HistoryAction.RestoreEvent -> restore(action.historyId)
        }
    }

    private fun restore(historyId: Long) {
        // Re-inserting refreshes the calendar; observeHistory() then re-validates the row automatically.
        viewModelScope.launch { runCatching { repository.restoreEvent(historyId) } }
    }

    private fun observe() {
        viewModelScope.launch {
            repository
                .observeHistory()
                .onEach { items -> setState { copy(items = items.toImmutableList(), isLoading = false) } }
                .collect()
        }
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.collect() = collect {}
