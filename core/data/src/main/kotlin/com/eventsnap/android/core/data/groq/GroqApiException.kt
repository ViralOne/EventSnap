package com.eventsnap.android.core.data.groq

import java.io.IOException

/**
 * A non-2xx answer from Groq, carrying the message Groq actually sent instead of Retrofit's opaque
 * `HTTP 404 Not Found`. Extends IOException so it travels the path suspend Retrofit calls already
 * use, and so the capture screen can keep showing `throwable.message`.
 */
class GroqApiException(
    val statusCode: Int,
    val groqCode: String?,
    val groqMessage: String?,
    userMessage: String,
) : IOException(userMessage) {
    /**
     * True when Groq rejected the *model id* — a retired, renamed or gated model. The caller
     * retries with the next candidate rather than surfacing a dead end (see CaptureRepositoryImpl).
     */
    val isModelUnavailable: Boolean
        get() =
            groqCode == MODEL_NOT_FOUND ||
                (statusCode == HTTP_NOT_FOUND && groqMessage?.contains("model", ignoreCase = true) == true) ||
                groqMessage?.contains("does not exist", ignoreCase = true) == true ||
                groqMessage?.contains("decommissioned", ignoreCase = true) == true

    companion object {
        const val HTTP_NOT_FOUND = 404
        private const val MODEL_NOT_FOUND = "model_not_found"
    }
}
