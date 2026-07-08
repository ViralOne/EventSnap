package com.eventsnap.android.core.data.groq

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Builds the system prompt that tells Groq to return a strict JSON envelope of events. Includes
 * the current local date/time and timezone so relative phrasing ("next Friday", "tomorrow 8pm")
 * resolves correctly.
 */
object EventPromptBuilder {
    fun systemPrompt(now: ZonedDateTime = ZonedDateTime.now()): String {
        val nowIso = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val zone: ZoneId = now.zone
        return """
            You extract calendar events from the user's text or image.
            The current local date-time is $nowIso (timezone: $zone).
            Resolve all relative dates/times against this reference.

            Respond with ONLY a JSON object of this exact shape:
            {
              "events": [
                {
                  "title": "string",
                  "start": "ISO-8601 local datetime, e.g. 2026-07-10T20:00:00",
                  "end": "ISO-8601 local datetime (default 1h after start if unknown)",
                  "allDay": false,
                  "location": "string or null",
                  "description": "string or null",
                  "reminderMinutesBefore": 30
                }
              ]
            }

            Rules:
            - Return one entry per distinct event; a schedule/poster may contain several.
            - If a field is unknown, use null (or omit reminderMinutesBefore).
            - Do not wrap the JSON in markdown fences or add commentary.
            """.trimIndent()
    }

    const val TEXT_INSTRUCTION: String = "Extract the event(s) from this description:"
    const val IMAGE_INSTRUCTION: String = "Extract the event(s) shown in this image (read any visible text)."
}
