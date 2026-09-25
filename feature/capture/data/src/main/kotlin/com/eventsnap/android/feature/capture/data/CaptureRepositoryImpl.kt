package com.eventsnap.android.feature.capture.data

import android.util.Base64
import com.eventsnap.android.core.data.groq.EventPromptBuilder
import com.eventsnap.android.core.data.groq.GroqApi
import com.eventsnap.android.core.data.groq.GroqApiException
import com.eventsnap.android.core.data.groq.GroqContentPart
import com.eventsnap.android.core.data.groq.GroqEventDto
import com.eventsnap.android.core.data.groq.GroqEventEnvelope
import com.eventsnap.android.core.data.groq.GroqImageUrl
import com.eventsnap.android.core.data.groq.GroqMessage
import com.eventsnap.android.core.data.groq.GroqModelCatalog
import com.eventsnap.android.core.data.groq.GroqModelRegistry
import com.eventsnap.android.core.data.groq.GroqRequest
import com.eventsnap.android.core.data.settings.SettingsStore
import com.eventsnap.android.core.model.AiModelArm
import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.CaptureInput
import com.eventsnap.android.core.model.Recurrence
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** How many different models one capture may try before giving up. */
private const val MAX_MODEL_ATTEMPTS = 3

internal class CaptureRepositoryImpl(
    private val groqApi: GroqApi,
    private val settingsStore: SettingsStore,
    private val modelRegistry: GroqModelRegistry,
    private val moshi: Moshi,
) : CaptureRepository {
    override val hasApiKey: Flow<Boolean> = settingsStore.groqApiKey.map { !it.isNullOrBlank() }

    override suspend fun extractEvents(input: CaptureInput): List<CalendarEvent> {
        val apiKey = settingsStore.groqApiKey.first()
        require(!apiKey.isNullOrBlank()) { "No Groq API key set. Add one in Settings." }

        val arm = if (input is CaptureInput.Image) AiModelArm.VISION else AiModelArm.TEXT
        val rawJson = requestWithModelFallback(apiKey, arm, userParts(input))

        val envelope =
            moshi.adapter(GroqEventEnvelope::class.java).fromJson(stripJsonFences(rawJson))
                ?: error("Could not parse the AI response.")

        return envelope.events.mapNotNull { dto ->
            val title = dto.title?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val start = dto.start?.let(::parseFlexible) ?: return@mapNotNull null
            toCalendarEvent(dto, title, start)
        }
    }

    /** The user message: the description for text, the instruction plus a data URI for images. */
    private fun userParts(input: CaptureInput): List<GroqContentPart> =
        when (input) {
            is CaptureInput.Text -> {
                listOf(
                    GroqContentPart(type = "text", text = "${EventPromptBuilder.TEXT_INSTRUCTION}\n\n${input.description}"),
                )
            }

            is CaptureInput.Image -> {
                val base64 = Base64.encodeToString(input.bytes, Base64.NO_WRAP)
                listOf(
                    GroqContentPart(type = "text", text = EventPromptBuilder.IMAGE_INSTRUCTION),
                    GroqContentPart(type = "image_url", image_url = GroqImageUrl(url = "data:${input.mimeType};base64,$base64")),
                )
            }
        }

    /**
     * Sends the request, and when Groq rejects the model id itself — retired, renamed or gated —
     * retires that id and retries with the next candidate instead of surfacing the failure. This is
     * what stops a provider-side model shutdown from breaking captures until the app is updated.
     */
    private suspend fun requestWithModelFallback(
        apiKey: String,
        arm: AiModelArm,
        userParts: List<GroqContentPart>,
    ): String {
        val tried = mutableSetOf<String>()
        var lastFailure: GroqApiException? = null

        repeat(MAX_MODEL_ATTEMPTS) {
            val model = modelRegistry.resolve(arm)
            if (!tried.add(model)) return@repeat // No new candidate left to try.
            try {
                return callGroq(apiKey, model, arm, userParts)
            } catch (failure: GroqApiException) {
                if (!failure.isModelUnavailable) throw failure
                Timber.w("Groq rejected model %s (%s); trying the next candidate", model, failure.groqMessage)
                modelRegistry.markUnavailable(model)
                lastFailure = failure
            }
        }
        lastFailure?.let { throw it }
        error("No usable Groq model for this capture.")
    }

    private suspend fun callGroq(
        apiKey: String,
        model: String,
        arm: AiModelArm,
        userParts: List<GroqContentPart>,
    ): String {
        val request =
            GroqRequest(
                model = model,
                // Not every model accepts reasoning_effort, and the arms want different values.
                reasoning_effort = GroqModelCatalog.reasoningEffortFor(model, vision = arm == AiModelArm.VISION),
                messages =
                    listOf(
                        GroqMessage(
                            role = "system",
                            content = listOf(GroqContentPart(type = "text", text = EventPromptBuilder.systemPrompt())),
                        ),
                        GroqMessage(role = "user", content = userParts),
                    ),
            )
        val response = groqApi.chatCompletions(authorization = "Bearer $apiKey", request = request)
        return response.choices
            .firstOrNull()
            ?.message
            ?.content
            ?: error("Groq returned an empty response.")
    }

    private fun toCalendarEvent(
        dto: GroqEventDto,
        title: String,
        start: ParsedTime,
    ): CalendarEvent {
        val end = dto.end?.let(::parseFlexible)
        val isTask = dto.isTask == true
        // A date-only value (no clock time) means an all-day event, even if the model didn't set the flag.
        // A task with a specific time ("buy bananas today 9") is a timed reminder — keep its clock slot;
        // only date-only tasks become all-day (via start.dateOnly). Don't force all-day just because it's a task.
        val allDay = dto.allDay == true || start.dateOnly
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
            recurrence = parseRecurrence(dto.recurrence),
        )
    }

    private fun parseRecurrence(value: String?): Recurrence =
        when (value?.trim()?.lowercase()) {
            "daily" -> Recurrence.DAILY
            "weekly" -> Recurrence.WEEKLY
            "monthly" -> Recurrence.MONTHLY
            "yearly" -> Recurrence.YEARLY
            else -> Recurrence.NONE
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
