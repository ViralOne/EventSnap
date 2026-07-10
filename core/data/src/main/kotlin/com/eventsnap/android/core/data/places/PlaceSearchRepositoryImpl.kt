package com.eventsnap.android.core.data.places

import com.eventsnap.android.core.model.PlaceSuggestion
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class PlaceSearchRepositoryImpl(
    private val photonApi: PhotonApi,
    private val deviceLocationProvider: DeviceLocationProvider,
) : PlaceSearchRepository {
    // Catch broadly: a geocoder failure (no network, rate limit, bad JSON) should degrade to "no
    // suggestions", never crash the review screen. The user can still type the location by hand.
    @Suppress("TooGenericExceptionCaught")
    override suspend fun search(query: String): List<PlaceSuggestion> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) return emptyList()
        return runCatching { smartSearch(trimmed) }
            .getOrDefault(emptyList())
            .distinctBy { it.storedValue }
            .take(MAX_RESULTS)
    }

    /**
     * Detects category keywords in the query (mall, restaurant, etc.) and runs TWO searches in
     * parallel: one filtered to the matching OSM tag (with the keyword stripped from the text) and
     * one plain (full text, no filter). Category-filtered results come first — this is what lets a
     * "mall downtown" query find the actual shopping centre even when its name doesn't contain "mall".
     */
    private suspend fun smartSearch(query: String): List<PlaceSuggestion> {
        // Coarse last-known location: lat/lon biases ranking; the reverse-geocoded city, appended to
        // the query, is the STRONGER signal — it stops same-named places abroad from winning. The
        // country code lets us hard-drop results outside the user's country.
        val loc = deviceLocationProvider.lastKnownLocation()
        val here = loc?.let { localeFor(it) }
        val city = here?.city
        // Append the city only when the user didn't already name a place there.
        val biased = if (city != null && !query.contains(city, ignoreCase = true)) "$query $city" else query
        val features = fetchFeatures(biased, loc)
        return features
            .filter { keepForCountry(it, here?.countryCode) }
            .mapNotNull(::toSuggestion)
    }

    /** Runs the category-filtered + plain searches (parallel) and concatenates their raw features. */
    private suspend fun fetchFeatures(
        query: String,
        loc: DeviceLocation?,
    ): List<PhotonFeature> {
        val match = detectCategory(query)
        if (match == null) {
            // No category word — single plain search.
            return runCatching { photonApi.search(query, lat = loc?.lat, lon = loc?.lon).features }
                .getOrDefault(emptyList())
        }
        // Parallel: category-filtered results first, then the plain fallback.
        return coroutineScope {
            val filtered =
                async {
                    runCatching { photonApi.search(match.cleanedQuery, osmTag = match.osmTag, lat = loc?.lat, lon = loc?.lon).features }
                        .getOrDefault(emptyList())
                }
            val plain =
                async {
                    runCatching { photonApi.search(query, lat = loc?.lat, lon = loc?.lon).features }
                        .getOrDefault(emptyList())
                }
            filtered.await() + plain.await()
        }
    }

    /** Keeps a feature only if we don't know the user's country, or its country matches. */
    private fun keepForCountry(
        feature: PhotonFeature,
        countryCode: String?,
    ): Boolean =
        countryCode == null ||
            feature.properties?.countrycode?.equals(countryCode, ignoreCase = true) == true

    /** The user's city + ISO country code, resolved from a coarse fix via Photon's reverse endpoint. */
    private data class LocaleHint(
        val city: String?,
        val countryCode: String?,
    )

    /**
     * Reverse-geocodes a coarse fix to the user's city + country via Photon's own reverse endpoint
     * (more reliable than Android's deprecated Geocoder). Returns null on any failure → unbiased search.
     */
    private suspend fun localeFor(loc: DeviceLocation): LocaleHint? =
        runCatching {
            val props =
                photonApi
                    .reverse(lat = loc.lat, lon = loc.lon)
                    .features
                    .firstOrNull()
                    ?.properties
            props?.let {
                LocaleHint(
                    city = (it.city ?: it.district)?.takeIf { c -> c.isNotBlank() },
                    countryCode = it.countrycode?.takeIf { c -> c.isNotBlank() },
                )
            }
        }.getOrNull()

    /** Result of detecting a category keyword: the cleaned query + the OSM tag to filter by. */
    private data class CategoryMatch(
        val cleanedQuery: String,
        val osmTag: String,
    )

    /**
     * Scans the query for a category keyword (RO + EN). If found, strips it from the text and
     * returns the corresponding Photon `osm_tag` filter value. Only matches if the remaining text
     * still has at least [MIN_QUERY_LENGTH] characters (otherwise the category-only query is useless).
     */
    private fun detectCategory(query: String): CategoryMatch? {
        val lower = query.lowercase()
        return CATEGORY_MAP
            .flatMap { (keywords, tag) -> keywords.map { keyword -> keyword to tag } }
            .firstNotNullOfOrNull { (keyword, tag) ->
                val cleaned = lower.replace(keyword, "").trim()
                cleaned
                    .takeIf { lower.contains(keyword) && it.length >= MIN_QUERY_LENGTH }
                    ?.let { CategoryMatch(cleanedQuery = it, osmTag = tag) }
            }
    }

    @Suppress("ReturnCount") // Multiple null-guards for missing geocoder fields are clearest as early returns.
    private fun toSuggestion(feature: PhotonFeature): PlaceSuggestion? {
        val props = feature.properties ?: return null
        val street = listOfNotNull(props.street, props.housenumber).joinToString(" ").ifBlank { null }
        // Headline: the venue name, else the street (a plain address with no named POI).
        val primary = props.name ?: street ?: return null
        // Address line: everything that disambiguates, minus whatever we already used as the headline.
        val secondary =
            listOfNotNull(
                street.takeIf { it != primary },
                props.city ?: props.district,
                props.state,
                props.country,
            ).distinct().joinToString(", ")
        return PlaceSuggestion(primary = primary, secondary = secondary)
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 3
        const val MAX_RESULTS = 6

        // Category keywords → OSM tag. Longest keywords first so "centru comercial" matches before
        // "centru". Each pair: list of trigger words (RO + EN) to a Photon osm_tag value.
        val CATEGORY_MAP: List<Pair<List<String>, String>> =
            listOf(
                listOf("centru comercial", "mall", "shopping") to "shop:mall",
                listOf("restaurant", "restaurante") to "amenity:restaurant",
                listOf("cafenea", "cafe", "coffee") to "amenity:cafe",
                listOf("farmacie", "pharmacy") to "amenity:pharmacy",
                listOf("benzinarie", "benzinărie", "gas station", "petrol") to "amenity:fuel",
                listOf("spital", "hospital") to "amenity:hospital",
                listOf("hotel", "hostel") to "tourism:hotel",
                listOf("parc", "park") to "leisure:park",
                listOf("supermarket", "hypermarket", "magazin") to "shop:supermarket",
                listOf("scoala", "școală", "school") to "amenity:school",
                listOf("universitate", "universitatea", "university") to "amenity:university",
                listOf("cinema", "cinematograf") to "amenity:cinema",
                listOf("banca", "bancă", "bank") to "amenity:bank",
                listOf("biblioteca", "bibliotecă", "library") to "amenity:library",
                listOf("aeroport", "airport") to "aeroway:aerodrome",
                listOf("gara", "gară", "train station") to "building:train_station",
                listOf("church", "biserica", "biserică") to "amenity:place_of_worship",
                listOf("gym", "sala", "fitness") to "leisure:fitness_centre",
            )
    }
}
