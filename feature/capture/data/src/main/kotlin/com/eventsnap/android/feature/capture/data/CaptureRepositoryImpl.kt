package com.eventsnap.android.feature.capture.data

import android.util.Base64
import com.eventsnap.android.core.data.groq.EventPromptBuilder
import com.eventsnap.android.core.data.groq.GroqApi
import com.eventsnap.android.core.data.groq.GroqContentPart
import com.eventsnap.android.core.data.groq.GroqEventDto
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
import java.time.LocalDate
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
        // reasoningEffort is set only for the text (gpt-oss) arm; the vision model ignores it.
        val (model, reasoningEffort, userParts) =
            when (input) {
                is CaptureInput.Text ->
                    Triple(
                        GroqModelCatalog.TEXT_MODEL,
                        GroqModelCatalog.TEXT_REASONING_EFFORT,
                        listOf(
                            GroqContentPart(type = "text", text = "${EventPromptBuilder.TEXT_INSTRUCTION}\n\n${input.description}"),
                        ),
                    )
                is CaptureInput.Image -> {
                    val base64 = Base64.encodeToString(input.bytes, Base64.NO_WRAP)
                    val dataUri = "data:${input.mimeType};base64,$base64"
                    Triple(
                        GroqModelCatalog.VISION_MODEL,
                        null,
                        listOf(
                            GroqContentPart(type = "text", text = EventPromptBuilder.IMAGE_INSTRUCTION),
                            GroqContentPart(type = "image_url", image_url = GroqImageUrl(url = dataUri)),
                        ),
                    )
                }
            }

        val request =
            GroqRequest(
                model = model,
                reasoning_effort = reasoningEffort,
                messages =
                    listOf(
                        GroqMessage(role = "system", content = listOf(GroqContentPart(type = "text", text = systemPrompt))),
                        GroqMessage(role = "user", content = userParts),
                    ),
            )

        val response = groqApi.chatCompletions(authorization = "Bearer $apiKey", request = request)
        val rawJson =
            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?: error("Groq returned an empty response.")

        val envelope =
            moshi.adapter(GroqEventEnvelope::class.java).fromJson(stripJsonFences(rawJson))
                ?: error("Could not parse the AI response.")

        return envelope.events.mapNotNull { dto ->
            val title = dto.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val start = dto.start?.let(::parseFlexible) ?: return@mapNotNull null
            toCalendarEvent(dto, title, start)
        }
    }

    private fun toCalendarEvent(
        dto: GroqEventDto,
        title: String,
        start: ParsedTime,
    ): CalendarEvent {
        val end = dto.end?.let(::parseFlexible)
        val isTask = dto.isTask == true
        // A date-only value (no clock time) means an all-day event, even if the model didn't set the flag.
        // Tasks are to-dos with a deadline, not a time slot, so they're all-day too.
        val allDay = dto.allDay == true || start.dateOnly || isTask
        val zone = ZoneId.systemDefault()
        val startMillis =
            start.dateTime
                .atZone(zone)
                .toInstant()
                .toEpochMilli()
        val endMillis =
            if (allDay) {
                // The model gives the INCLUSIVE last day; CalendarProvider's DTEND is exclusive,
                // so add one day. Single-day events (end == start/null) become a 1-day block.
                val lastDay = (end ?: start).dateTime
                val exclusiveEnd = maxOf(lastDay, start.dateTime).plusDays(1)
                exclusiveEnd.atZone(zone).toInstant().toEpochMilli()
            } else {
                val explicitEnd =
                    end
                        ?.dateTime
                        ?.atZone(zone)
                        ?.toInstant()
                        ?.toEpochMilli()
                explicitEnd?.takeIf { it > startMillis } ?: (startMillis + Duration.ofHours(1).toMillis())
            }
        return CalendarEvent(
            title = title,
            startEpochMillis = startMillis,
            endEpochMillis = endMillis,
            allDay = allDay,
            location = dto.location?.takeIf { it.isNotBlank() },
            description = dto.description?.takeIf { it.isNotBlank() },
            reminderMinutesBefore = dto.reminderMinutesBefore,
            isTask = isTask,
        )
    }

    /** A parsed instant plus whether the source had only a date (→ all-day). */
    private data class ParsedTime(
        val dateTime: LocalDateTime,
        val dateOnly: Boolean,
    )

    private fun parseFlexible(value: String): ParsedTime? {
        val trimmed = value.trim()
        // Try, in order: ISO local datetime, lenient datetime, then date-only (→ all-day).
        val asDateTime =
            runCatching { LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }.getOrNull()
                ?: runCatching { LocalDateTime.parse(trimmed) }.getOrNull()
        if (asDateTime != null) return ParsedTime(asDateTime, dateOnly = false)

        val asDate = runCatching { LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()
        return asDate?.let { ParsedTime(it.atStartOfDay(), dateOnly = true) }
    }

    /** Groq sometimes wraps JSON in ```json … ``` fences; strip them before parsing. */
    private fun stripJsonFences(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("```")) return trimmed
        return trimmed
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}
