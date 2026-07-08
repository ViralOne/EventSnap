package com.eventsnap.android.feature.capture.mvi

import com.eventsnap.android.core.ViewAction
import com.eventsnap.android.core.ViewSideEffect
import com.eventsnap.android.core.ViewState
import com.eventsnap.android.core.model.CaptureInput

data class CaptureState(
    val description: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
) : ViewState

sealed interface CaptureAction : ViewAction {
    data class DescriptionChanged(
        val value: String,
    ) : CaptureAction

    data object SubmitText : CaptureAction

    data class SubmitImage(
        val input: CaptureInput.Image,
    ) : CaptureAction

    data class SubmitSharedText(
        val text: String,
    ) : CaptureAction

    data object ErrorDismissed : CaptureAction
}

sealed interface CaptureEffect : ViewSideEffect {
    data object NavigateToReview : CaptureEffect
}
