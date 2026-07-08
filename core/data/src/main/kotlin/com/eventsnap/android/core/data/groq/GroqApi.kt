package com.eventsnap.android.core.data.groq

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Groq's OpenAI-compatible chat-completions endpoint. One call handles both arms:
 * text descriptions (fast text model) and images (vision model) — the content array
 * carries either plain text or an image_url data URI.
 */
interface GroqApi {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Header("Authorization") authorization: String,
        @Body request: GroqRequest,
    ): GroqResponse
}

@JsonClass(generateAdapter = true)
data class GroqRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.2,
    @Suppress("ConstructorParameterNaming")
    val response_format: GroqResponseFormat? = GroqResponseFormat(),
)

@JsonClass(generateAdapter = true)
data class GroqResponseFormat(
    val type: String = "json_object",
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    val role: String,
    val content: List<GroqContentPart>,
)

@JsonClass(generateAdapter = true)
data class GroqContentPart(
    val type: String,
    val text: String? = null,
    @Suppress("ConstructorParameterNaming")
    val image_url: GroqImageUrl? = null,
)

@JsonClass(generateAdapter = true)
data class GroqImageUrl(
    val url: String,
)

@JsonClass(generateAdapter = true)
data class GroqResponse(
    val choices: List<GroqChoice> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    val message: GroqResponseMessage?,
)

@JsonClass(generateAdapter = true)
data class GroqResponseMessage(
    val content: String?,
)
