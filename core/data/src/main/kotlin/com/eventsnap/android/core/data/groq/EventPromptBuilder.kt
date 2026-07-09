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
        val today = now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val weekday =
            now.dayOfWeek.name
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        val zone: ZoneId = now.zone
        return """
            You turn a person's quick, messy note into calendar events. People type fast: typos,
            abbreviations, no punctuation, lowercase, mixed languages. Read the INTENT, not the
            literal words, and be helpful — fill obvious gaps with sensible defaults, but never
            invent details that aren't implied.

            Reference: right now it is $nowIso — $weekday, $today (timezone: $zone).
            Resolve every relative date/time against this. Pick the NEAREST FUTURE occurrence
            (if "Monday 9am" already passed this week, use next Monday). Never output a date in
            the past unless the user clearly states a past date.

            Respond with ONLY a JSON object of this exact shape (no markdown fences, no prose):
            {
              "events": [
                {
                  "title": "short human title",
                  "start": "2026-07-10T20:00:00",
                  "end": "2026-07-10T21:00:00",
                  "allDay": false,
                  "location": "string or null",
                  "description": "string or null",
                  "reminderMinutesBefore": 30,
                  "isTask": false,
                  "recurrence": "none"
                }
              ]
            }

            LANGUAGE:
            - The note may be in any language (Romanian, English, …). Keep "title", "location"
              and "description" IN THE SAME LANGUAGE the user wrote. Do NOT translate them.
            - Clean the title: fix typos, expand abbreviations, capitalize sensibly
              ("dinnr w sara" → "Dinner with Sara"; "sedinta cu Ana" → "Ședință cu Ana").

            EVENT vs TASK ("isTask"):
            - EVENT (isTask false): something that happens at a time/place, that the user attends or
              observes — meeting, appointment, concert, flight, birthday, party.
            - TASK (isTask true): something the user must DO, an action item with a deadline but no
              real time slot — "buy a gift", "renew passport", "pay rent by the 1st", "call the plumber".
              Verbs of doing (buy, call, send, pay, submit, renew, fix, prepare) usually mean a task.
            - When it's a task, set "isTask": true, use the DEADLINE as the date, and prefer
              date-only + "allDay": true (a task has no clock slot unless the user gives a specific time).
            - When unsure, treat it as an event (isTask false). Default is false.

            MULTIPLE EVENTS:
            - Return one entry per distinct event. One note can hold several
              ("free 28-31 July and her birthday is the 30th" = TWO events).

            DATES & TIMES:
            - Timed event → "start"/"end" as "YYYY-MM-DDTHH:mm:ss", "allDay": false.
            - All-day or multi-day (birthdays, holidays, "free 28-31", a day with no clock time)
              → date-only "YYYY-MM-DD" and "allDay": true. Use REAL calendar dates only.
              Set start = first day and end = LAST day (both inclusive, as written):
              "free 28-31 July" → start 2026-07-28, end 2026-07-31; a single day → start = end.
              Do NOT add a day yourself and never output an impossible date like 2026-07-32.
            - Vague times: morning→09:00, noon→12:00, afternoon→15:00, evening→19:00, night→21:00.
            - Duration: if only a start time is given, default end = start + 1 hour. If a range is
              given ("9 to 5", "3-4pm") use it. Meetings/calls default 30–60 min, meals ~1h.

            OTHER FIELDS:
            - location: only if a place is mentioned; else null. Don't guess an address.
            - reminderMinutesBefore: 30 unless the user asks otherwise ("remind me 1h before"→60,
              "the day before"→1440). Use null only if they say no reminder.
            - Recurrence: set "recurrence" to one of none/daily/weekly/monthly/yearly.
              "every Monday" or "weekly" → weekly; "every day"/"daily" → daily; "monthly" → monthly;
              "every year"/birthdays → yearly; a one-off → none. Set "start" to the FIRST upcoming
              occurrence. Default "none" when there's no repeat.

            NO EVENT:
            - If the note has no schedulable event (chit-chat, "ok thanks", a random sentence),
              return {"events": []}. Never fabricate an event just to return something.
            """.trimIndent()
    }

    const val TEXT_INSTRUCTION: String = "Turn this note into calendar event(s):"
    const val IMAGE_INSTRUCTION: String =
        "Read this image (poster, ticket, screenshot, schedule) and extract the event(s). " +
            "Keep any text in its original language."
}
