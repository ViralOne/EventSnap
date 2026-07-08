package com.eventsnap.android.feature.capture.data

import android.util.Base64
import com.eventsnap.android.core.data.groq.EventPromptBuilder
import com.eventsnap.android.core.data.groq.GroqApi
import com.eventsnap.android.core.data.groq.GroqContentPart
import com.eventsnap.android.core.data.groq.GroqEventEnvelope
import com.eventsnap.android.core.data.groq.GroqImageUrl
import com.eventsnap.android.core.data.groq.GroqMessage
import com.eventsnap.android.core.data.groq.GroqModelCatalog
import com.eventsnap.android.core.data.groq.GroqRequest
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.CaptureInput
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class CaptureRepositoryImpl(
    private val groqApi: GroqApi,
    private val settingsStore: SettingsStore,
    private val moshi: Moshi,
) : CaptureRepository {
    override suspend fun extractEvents(input: CaptureInput): List<CalendarEvent> {
        val apiKey = settingsStore.groqApiKey.first()
        require(!apiKey.isNullOrBlank()) { "No Groq API key set. Add one in Settings." }

        val systemPrompt = EventPromptBuilder.systemPrompt()
        val (model, userParts) =
            when (input) {
                is CaptureInput.Text ->
                    GroqModelCatalog.TEXT_MODEL to
                        listOf(
                            GroqContentPart(type = "text", text = "${EventPromptBuilder.TEXT_INSTRUCTION}\n\n${input.description}"),
                        )
                is CaptureInput.Image -> {
                    val base64 = Base64.encodeToString(input.bytes, Base64.NO_WRAP)
                    val dataUri = "data:${input.mimeType};base64,$base64"
                    GroqModelCatalog.VISION_MODEL to
                        listOf(
                            GroqContentPart(type = "text", text = EventPromptBuilder.IMAGE_INSTRUCTION),
                            GroqContentPart(type = "image_url", image_url = GroqImageUrl(url = dataUri)),
                        )
                }
            }

        val request =
            GroqRequest(
                model = model,
                messages =
                    listOf(
                        GroqMessage(role = "system", content = listOf(GroqContentPart(type = "text", text = systemPrompt))),
                        GroqMessage(role = "user", content = userParts),
                    ),
            )

        val response = groqApi.chatCompletions(authorization = "Bearer $apiKey", request = request)
        val json =
            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?: error("Groq returned an empty response.")

        val envelope =
            moshi.adapter(GroqEventEnvelope::class.java).fromJson(json)
                ?: error("Could not parse the AI response.")

        return envelope.events.mapNotNull { dto ->
            val title = dto.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val start = dto.start?.let(::parseLocal) ?: return@mapNotNull null
            val startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis =
                dto.end
                    ?.let(::parseLocal)
                    ?.atZone(ZoneId.systemDefault())
                    ?.toInstant()
                    ?.toEpochMilli()
                    ?: (startMillis + Duration.ofHours(1).toMillis())
            CalendarEvent(
                title = title,
                startEpochMillis = startMillis,
                endEpochMillis = endMillis,
                allDay = dto.allDay ?: false,
                location = dto.location?.takeIf { it.isNotBlank() },
                description = dto.description?.takeIf { it.isNotBlank() },
                reminderMinutesBefore = dto.reminderMinutesBefore,
            )
        }
    }

    private fun parseLocal(value: String): LocalDateTime? =
        runCatching {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }.getOrElse {
            runCatching { LocalDateTime.parse(value) }.getOrNull()
        }
}
