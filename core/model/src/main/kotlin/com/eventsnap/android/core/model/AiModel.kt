package com.eventsnap.android.core.model

/**
 * One model offered by the AI provider right now, as reported by its live model list.
 *
 * [supportsVision] is inferred, not reported: Groq's `/models` response carries no modality field,
 * so it is a best guess from the model id. Treat it as a hint for sorting/labelling the picker,
 * not a guarantee.
 */
data class AiModel(
    val id: String,
    val ownedBy: String? = null,
    val contextWindow: Int? = null,
    val supportsVision: Boolean = false,
)

/** Which capture arm a model selection applies to. */
enum class AiModelArm {
    /** Typed or shared text descriptions. */
    TEXT,

    /** Photos, screenshots and PDFs — needs a multimodal model. */
    VISION,
}
