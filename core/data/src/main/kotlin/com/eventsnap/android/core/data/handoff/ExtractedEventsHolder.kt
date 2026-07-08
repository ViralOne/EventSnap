package com.eventsnap.android.core.data.handoff

import com.eventsnap.android.core.model.CalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory handoff of AI-extracted events from the capture feature to the review feature.
 * Keeps navigation routes parameterless — capture writes the batch, review reads and clears it.
 * Process-death loses the batch, which is fine: the user simply re-captures.
 */
class ExtractedEventsHolder {
    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events: StateFlow<List<CalendarEvent>> = _events.asStateFlow()

    fun set(events: List<CalendarEvent>) {
        _events.value = events
    }

    fun consume(): List<CalendarEvent> = _events.value.also { _events.value = emptyList() }
}
