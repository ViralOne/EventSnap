package com.eventsnap.android.feature.capture

import androidx.lifecycle.viewModelScope
import com.eventsnap.android.core.BaseViewModel
import com.eventsnap.android.core.data.handoff.ExtractedEventsHolder
import com.eventsnap.android.core.model.CaptureInput
import com.eventsnap.android.feature.capture.data.CaptureRepository
import com.eventsnap.android.feature.capture.mvi.CaptureAction
import com.eventsnap.android.feature.capture.mvi.CaptureEffect
import com.eventsnap.android.feature.capture.mvi.CaptureState
import kotlinx.coroutines.launch

class CaptureViewModel(
    private val repository: CaptureRepository,
    private val extractedEventsHolder: ExtractedEventsHolder,
) : BaseViewModel<CaptureState, CaptureAction, CaptureEffect>(CaptureState()) {
    override suspend fun onAction(action: CaptureAction) {
        when (action) {
            is CaptureAction.DescriptionChanged -> setState { copy(description = action.value) }
            is CaptureAction.SubmitText -> extract(CaptureInput.Text(state.value.description))
            is CaptureAction.SubmitSharedText -> {
                setState { copy(description = action.text) }
                extract(CaptureInput.Text(action.text))
            }
            is CaptureAction.SubmitImage -> extract(action.input)
            is CaptureAction.ErrorDismissed -> setState { copy(error = null) }
        }
    }

    private fun extract(input: CaptureInput) {
        if (input is CaptureInput.Text && input.description.isBlank()) {
            setState { copy(error = "Type a description first.") }
            return
        }
        setState { copy(isProcessing = true, error = null) }
        viewModelScope.launch {
            runCatching { repository.extractEvents(input) }
                .onSuccess { events ->
                    if (events.isEmpty()) {
                        setState { copy(isProcessing = false, error = "No events found. Try rephrasing or a clearer image.") }
                    } else {
                        extractedEventsHolder.set(events)
                        setState { copy(isProcessing = false) }
                        setEffect(CaptureEffect.NavigateToReview)
                    }
                }.onFailure { throwable ->
                    setState { copy(isProcessing = false, error = throwable.message ?: "Something went wrong.") }
                }
        }
    }
}
