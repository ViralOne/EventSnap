package com.eventsnap.android.feature.capture.components

/**
 * Example prompts shown as the input placeholder to teach people how to phrase an event.
 * One is picked at random each time the capture screen appears (see [rememberRandomPlaceholder]).
 * Kept multilingual (the app's audience writes in Romanian and English) to hint that either works.
 */
internal val CAPTURE_PLACEHOLDERS: List<String> =
    listOf(
        "Dentist Tuesday 3pm",
        "Coffee with Ana next Friday at 10",
        "Ramona's birthday July 30",
        "Flight to Cluj Sat 6:40am",
        "Team standup every Monday 9am",
        "Pay rent by the 1st",
        "Concert Saturday 8pm at Arenele Romane",
        "Ședință cu echipa mâine la 14",
        "Vacanță 28-31 iulie",
        "Sună instalatorul până vineri",
    )
