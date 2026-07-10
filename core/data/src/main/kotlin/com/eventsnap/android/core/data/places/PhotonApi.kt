package com.eventsnap.android.core.data.places

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Photon (photon.komoot.io) — a keyless geocoder over OpenStreetMap. No API key, no billing.
 * Returns GeoJSON: a list of features whose `properties` describe each place. We only read the
 * naming/address fields to build a clean location string.
 */
interface PhotonApi {
    @GET("api")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("lang") lang: String = "en",
        @Query("osm_tag") osmTag: String? = null,
        // Optional location bias: ranks results near this lat/lon first (the user's coarse position).
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
    ): PhotonResponse

    /** Reverse-geocode a coarse fix to a place (used to read the user's city — more reliable than
     *  Android's deprecated Geocoder). */
    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
    ): PhotonResponse
}

@JsonClass(generateAdapter = true)
data class PhotonResponse(
    val features: List<PhotonFeature> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class PhotonFeature(
    val properties: PhotonProperties? = null,
)

@JsonClass(generateAdapter = true)
data class PhotonProperties(
    val name: String? = null,
    val street: String? = null,
    @Suppress("ConstructorParameterNaming")
    val housenumber: String? = null,
    val city: String? = null,
    val district: String? = null,
    val state: String? = null,
    val country: String? = null,
    /** ISO 3166-1 alpha-2 code (e.g. "RO", "US"); used to keep results within the user's country. */
    val countrycode: String? = null,
    val postcode: String? = null,
)
