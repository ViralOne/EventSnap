package com.eventsnap.android.core.data.places

import com.eventsnap.android.core.model.PlaceSuggestion

/** Looks up real-world places for the location autocomplete. Keyless (OpenStreetMap via Photon). */
interface PlaceSearchRepository {
    /** Returns up to a handful of place suggestions for [query], or empty on blank/short/errors. */
    suspend fun search(query: String): List<PlaceSuggestion>
}
