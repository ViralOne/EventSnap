package com.eventsnap.android.core.data.groq

/**
 * Model preferences for each capture arm.
 *
 * Nothing here is used blindly: GroqModelRegistry intersects these lists with Groq's live
 * `GET /models` response, so a retired id is skipped instead of producing an HTTP 404. The lists
 * are only a preference order plus an offline fallback for when the model list can't be fetched.
 *
 * TEXT: openai/gpt-oss-120b — was the most accurate on messy, multilingual, typo-ridden input in
 *   live testing. It is a reasoning model, so requests must use reasoning_effort=low and a generous
 *   max_completion_tokens (see GroqRequest) or it can run out of tokens before finishing the JSON.
 *
 * VISION: qwen/qwen3.8-27b — replaces meta-llama/llama-4-scout-17b-16e-instruct, which Groq shut
 *   down on 2026-07-17 (https://console.groq.com/docs/deprecations); calls to the retired id came
 *   back as HTTP 404 (model_not_found). gpt-oss models do NOT accept images (verified: they reject
 *   array/image content), so the image arm needs its own multimodal model. Groq limits: 3 images
 *   per request, 20 MB per request, 2048 input tokens per image.
 */
object GroqModelCatalog {
    const val TEXT_MODEL: String = "openai/gpt-oss-120b"
    const val VISION_MODEL: String = "qwen/qwen3.8-27b"

    /** Reasoning effort sent for the text (gpt-oss) arm. */
    const val TEXT_REASONING_EFFORT: String = "low"

    /**
     * Qwen3.8 is a hybrid thinking/instruct model and thinks by default. Reading a ticket is a
     * transcription task, not a reasoning one, so the vision arm runs in instruct mode
     * (reasoning_effort=none): lower latency, and reasoning tokens can't eat the
     * max_completion_tokens budget before the JSON is finished.
     *
     * Only sent for models known to accept it — see [acceptsReasoningEffort].
     */
    const val VISION_REASONING_EFFORT: String = "none"

    /** Text models in descending preference; the first one Groq still serves wins. */
    val TEXT_PREFERENCES: List<String> =
        listOf(
            TEXT_MODEL,
            "openai/gpt-oss-20b",
            "qwen/qwen3.8-27b",
        )

    /** Vision-capable models in descending preference. */
    val VISION_PREFERENCES: List<String> =
        listOf(
            VISION_MODEL,
            "qwen/qwen3.6-27b",
        )

    /**
     * Substrings that mark a model id as multimodal. Groq's `/models` response has no modality
     * field, so this is how a vision model Groq adds *after* this release still gets found
     * automatically instead of waiting for an app update.
     */
    private val VISION_ID_HINTS: List<String> =
        listOf("qwen3.8", "qwen3.6", "-vl", "vl-", "vision", "multimodal", "llama-4", "scout", "maverick", "gemma-3", "pixtral", "llava")

    /** Ids that reject the `reasoning_effort` parameter outright, or the value we'd send. */
    private val NO_REASONING_EFFORT_HINTS: List<String> = listOf("llama-3", "llama-4", "gemma", "llava", "pixtral", "mixtral", "mistral")

    /** Best-effort guess at whether [modelId] accepts image content. */
    fun looksVisionCapable(modelId: String): Boolean {
        val id = modelId.lowercase()
        return VISION_PREFERENCES.any { it.equals(modelId, ignoreCase = true) } || VISION_ID_HINTS.any { it in id }
    }

    /**
     * Whether to send `reasoning_effort` to [modelId]. Non-reasoning models reject the parameter
     * with a 400, which matters now that the model can be anything the user picked from the live
     * list rather than the one id this app was written against.
     */
    fun acceptsReasoningEffort(modelId: String): Boolean {
        val id = modelId.lowercase()
        return NO_REASONING_EFFORT_HINTS.none { it in id }
    }

    /** The effort to send for [modelId] on the given arm, or null when it must be omitted. */
    fun reasoningEffortFor(
        modelId: String,
        vision: Boolean,
    ): String? =
        when {
            !acceptsReasoningEffort(modelId) -> null
            vision -> VISION_REASONING_EFFORT
            else -> TEXT_REASONING_EFFORT
        }
}
