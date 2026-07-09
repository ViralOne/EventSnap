package com.eventsnap.android.core.model

/**
 * A calendar event extracted by the AI and shown on the review screen before being written
 * to the device calendar. Times are epoch milliseconds (UTC). This is the single domain model
 * that flows: AI extraction → review/edit → CalendarProvider insert → local history.
 */
data class CalendarEvent(
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val allDay: Boolean = false,
    val location: String? = null,
    val description: String? = null,
    /** Minutes before start for a reminder, or null for no reminder. */
    val reminderMinutesBefore: Int? = null,
    /** IANA timezone id (e.g. "Europe/Bucharest"); null means the device default. */
    val timeZoneId: String? = null,
    /**
     * True when this is a to-do (something to *do*) rather than an event to *attend*. Tasks are
     * written to the calendar as an all-day reminder. The AI proposes a value; the user confirms
     * it with the checkbox on the review screen.
     */
    val isTask: Boolean = false,
    /** How often the event repeats. The AI proposes it; the user can change it on the review screen. */
    val recurrence: Recurrence = Recurrence.NONE,
)

/** Supported repeat frequencies. Maps to a CalendarProvider RRULE (RFC 5545). */
enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

/** A calendar the user can write events into, as reported by the CalendarProvider. */
data class TargetCalendar(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val isPrimary: Boolean,
)
