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

    /** Re-add a previously-deleted event back to the calendar (by history row id). */
    data class RestoreEvent(
        val historyId: Long,
    ) : HistoryAction
}

sealed interface HistoryEffect : ViewSideEffect
