package com.eventsnap.android.core.data.places

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat

/** A coarse lat/lon used to bias place search toward where the user is. */
data class DeviceLocation(
    val lat: Double,
    val lon: Double,
)

/**
 * Reads the device's last-known coarse location to bias place suggestions. Never triggers active
 * GPS — it only reads the cached last-known fix from each provider, so it's instant and battery-free.
 * Returns null when permission isn't granted or no fix is cached; callers fall back to unbiased search.
 */
class DeviceLocationProvider(
    private val context: Context,
) {
    fun lastKnownLocation(): DeviceLocation? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        // Prefer the network (coarse) provider; fall back to any provider with a cached fix.
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER, LocationManager.GPS_PROVIDER)
        return manager?.let { mgr ->
            providers.asSequence().mapNotNull { provider -> lastKnownFrom(mgr, provider) }.firstOrNull()
        }
    }

    // getLastKnownLocation can throw if the provider is disabled/absent; treat any failure as "no fix".
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    // Permission is verified by hasLocationPermission() before any call reaches here; the
    // getLastKnownLocation call is also wrapped in runCatching to absorb a SecurityException.
    @SuppressLint("MissingPermission")
    private fun lastKnownFrom(
        manager: LocationManager,
        provider: String,
    ): DeviceLocation? =
        runCatching {
            manager.getLastKnownLocation(provider)?.let { DeviceLocation(it.latitude, it.longitude) }
        }.getOrNull()

    private fun hasLocationPermission(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED
    }
}
