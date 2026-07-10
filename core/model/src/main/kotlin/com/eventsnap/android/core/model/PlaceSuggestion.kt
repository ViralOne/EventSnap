package com.eventsnap.android.core.model

/**
 * One place-search result. [primary] is the headline (venue or street), [secondary] the
 * disambiguating address line (city, region, country). [storedValue] is what we write into the
 * calendar's location field — a single clean string that Google Calendar geocodes into a map pin.
 */
data class PlaceSuggestion(
    val primary: String,
    val secondary: String,
) {
    val storedValue: String
        get() = if (secondary.isBlank()) primary else "$primary, $secondary"
}
