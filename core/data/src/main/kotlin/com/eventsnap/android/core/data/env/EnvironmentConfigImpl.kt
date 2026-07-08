package com.eventsnap.android.core.data.env

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eventsnap.android.core.data.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.envDataStore by preferencesDataStore(name = "env_config")
private val API_BASE_URL_KEY = stringPreferencesKey("api_base_url")

class EnvironmentConfigImpl(
    private val context: Context,
    private val scope: CoroutineScope,
    override val stagingUrl: String,
    override val prodUrl: String,
) : EnvironmentConfig {
    private val _apiBaseUrl = MutableStateFlow(BuildConfig.API_BASE_URL)
    override val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    init {
        scope.launch {
            val stored =
                context.envDataStore.data
                    .map { it[API_BASE_URL_KEY] }
                    .first()
            if (!stored.isNullOrBlank()) _apiBaseUrl.value = stored
        }
    }

    override suspend fun setApiBaseUrl(url: String) {
        _apiBaseUrl.value = url
        context.envDataStore.edit { it[API_BASE_URL_KEY] = url }
    }

    override suspend fun reset() {
        _apiBaseUrl.value = BuildConfig.API_BASE_URL
        context.envDataStore.edit { it.remove(API_BASE_URL_KEY) }
    }
}
