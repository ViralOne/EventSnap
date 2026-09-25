package com.eventsnap.android.core.data.groq

import com.eventsnap.android.core.model.AiModel
import com.eventsnap.android.core.model.AiModelArm

/**
 * Knows which models Groq is actually serving right now, and picks one per capture arm.
 *
 * Hardcoded ids rot: Groq retires models on a published schedule and a retired id answers with
 * HTTP 404, which used to reach the user as "HTTP 404 Not Found". This layer checks the live
 * `GET /models` list first, honours a model the user pinned in Settings, and falls back through a
 * preference order when the pinned or preferred id is gone.
 */
interface GroqModelRegistry {
    /**
     * Models Groq currently serves, vision-capable ones first. [forceRefresh] bypasses the cache
     * (the Settings refresh button). Returns an empty list when the list can't be fetched — callers
     * then fall back to the built-in preferences rather than blocking the user.
     */
    suspend fun availableModels(forceRefresh: Boolean = false): List<AiModel>

    /**
     * The id to send for [arm]: the user's pinned model if Groq still serves it, otherwise the
     * first live model from the arm's preference order, otherwise the built-in default.
     */
    suspend fun resolve(arm: AiModelArm): String

    /**
     * Records that [modelId] was rejected as unavailable, so [resolve] skips it for the rest of the
     * session and the next candidate is used instead.
     */
    fun markUnavailable(modelId: String)
}
