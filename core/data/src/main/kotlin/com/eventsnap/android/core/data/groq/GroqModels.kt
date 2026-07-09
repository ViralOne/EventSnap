package com.eventsnap.android.core.data.groq

import com.squareup.moshi.JsonClass

/**
 * The JSON envelope we instruct Groq to return: a list of events. Each field is nullable so a
 * partial extraction still parses — the review screen fills the gaps. Times are ISO-8601 local
 * strings; the mapper converts them to epoch millis in the domain model.
 */
@JsonClass(generateAdapter = true)
data class GroqEventEnvelope(
    val events: List<GroqEventDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class GroqEventDto(
    val title: String? = null,
    val start: String? = null,
    val end: String? = null,
    val allDay: Boolean? = null,
    val location: String? = null,
    val description: String? = null,
    val reminderMinutesBefore: Int? = null,
    val isTask: Boolean? = null,
    /** One of: none, daily, weekly, monthly, yearly. */
    val recurrence: String? = null,
)
