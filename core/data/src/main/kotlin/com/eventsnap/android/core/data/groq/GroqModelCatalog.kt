package com.eventsnap.android.core.data.groq

/**
 * Default Groq model ids for each capture arm.
 *
 * TEXT: openai/gpt-oss-120b — replaces llama-3.3-70b-versatile, which Groq deprecates on
 *   2026-08-16 (see https://console.groq.com/docs/deprecations). In live testing gpt-oss-120b
 *   was the most accurate on messy, multilingual, typo-ridden input. It is a reasoning model, so
 *   requests must use reasoning_effort=low and a generous max_completion_tokens (see GroqRequest)
 *   or it can run out of tokens before finishing the JSON.
 *
 * VISION: meta-llama/llama-4-scout-17b-16e-instruct — gpt-oss models do NOT accept images
 *   (verified: they reject array/image content), so the image arm stays on Llama 4 Scout.
 */
object GroqModelCatalog {
    const val TEXT_MODEL: String = "openai/gpt-oss-120b"
    const val VISION_MODEL: String = "meta-llama/llama-4-scout-17b-16e-instruct"

    /** Reasoning effort sent for the text (gpt-oss) arm. Vision arm leaves this null. */
    const val TEXT_REASONING_EFFORT: String = "low"
}
