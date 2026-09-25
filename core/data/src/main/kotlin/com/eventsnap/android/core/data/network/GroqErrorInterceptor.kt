package com.eventsnap.android.core.data.network

import com.eventsnap.android.core.data.groq.GroqApiException
import okhttp3.Interceptor
import okhttp3.Response
import org.json.JSONObject

/** Body bytes peeked from an error response — enough for Groq's error JSON, never a huge payload. */
private const val ERROR_BODY_PEEK_BYTES = 8L * 1024
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403

/**
 * Replaces Retrofit's opaque `HttpException: HTTP 404 Not Found` with the message Groq actually
 * sent. Groq answers failures with `{"error":{"message":"…","code":"…"}}`, and that message is the
 * only thing that distinguishes a retired model id from a bad key or an oversized image — all of
 * which the UI otherwise showed as a bare status line.
 */
class GroqErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val body = runCatching { response.peekBody(ERROR_BODY_PEEK_BYTES).string() }.getOrNull()
        val error = parseError(body)
        response.close()
        throw GroqApiException(
            statusCode = response.code,
            groqCode = error?.code,
            groqMessage = error?.message,
            userMessage = describe(response.code, error?.message),
        )
    }

    private fun describe(
        code: Int,
        message: String?,
    ): String =
        when {
            message.isNullOrBlank() -> "Groq request failed (HTTP $code)."

            // A 401/403 is almost always a key problem; point at the fix instead of the raw text.
            code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN -> "$message Check your Groq API key in Settings."

            else -> "$message (HTTP $code)"
        }

    /** Pulls Groq's error envelope out of the body; null when the body isn't that shape. */
    private fun parseError(body: String?): GroqError? =
        body?.takeIf { it.isNotBlank() }?.let { json ->
            runCatching {
                JSONObject(json).optJSONObject("error")?.let { error ->
                    GroqError(
                        message = error.optString("message").takeIf { it.isNotBlank() },
                        code = error.optString("code").takeIf { it.isNotBlank() },
                    )
                }
            }.getOrNull()
        }

    private data class GroqError(
        val message: String?,
        val code: String?,
    )
}
