package com.eventsnap.android.feature.history.mvi

import com.eventsnap.android.core.ViewAction
import com.eventsnap.android.core.ViewSideEffect
import com.eventsnap.android.core.ViewState
import com.eventsnap.android.feature.history.data.HistoryItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HistoryState(
    val items: ImmutableList<HistoryItem> = persistentListOf(),
    val isLoading: Boolean = true,
) : ViewState

sealed interface HistoryAction : ViewAction {
    data object Load : HistoryAction
}

sealed interface HistoryEffect : ViewSideEffect
