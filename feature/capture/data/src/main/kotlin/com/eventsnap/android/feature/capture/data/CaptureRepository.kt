package com.eventsnap.android.feature.capture.data

import com.eventsnap.android.core.model.CalendarEvent
import com.eventsnap.android.core.model.CaptureInput

interface CaptureRepository {
    /**
     * Sends [input] (text or image) to Groq and returns the extracted events. Throws on network
     * errors, a missing API key, or an unparseable response — the ViewModel maps that to an
     * error state.
     */
    suspend fun extractEvents(input: CaptureInput): List<CalendarEvent>
}
