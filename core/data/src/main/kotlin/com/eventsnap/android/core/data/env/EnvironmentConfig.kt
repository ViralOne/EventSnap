package com.eventsnap.android.core.data.env

import kotlinx.coroutines.flow.StateFlow

interface EnvironmentConfig {
    /** Live base URL — emits `BuildConfig.API_BASE_URL` until the user overrides it. */
    val apiBaseUrl: StateFlow<String>

    /** Pre-canned options the dialog uses for the Staging / Prod radio buttons. */
    val stagingUrl: String
    val prodUrl: String

    suspend fun setApiBaseUrl(url: String)

    /** Drop the override and fall back to `BuildConfig.API_BASE_URL`. */
    suspend fun reset()
}
