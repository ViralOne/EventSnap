package com.eventsnap.android.core.data.network

import com.eventsnap.android.core.data.env.EnvironmentConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Rewrites every outgoing request's scheme/host/port to match the current EnvironmentConfig URL.
 * The request path/query stays as the caller built it; only the origin is swapped. This lets the
 * Retrofit base URL stay constant (the BuildConfig default) while runtime switches honor the dialog.
 */
class EnvironmentBaseUrlInterceptor(
    private val environmentConfig: EnvironmentConfig,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val current = environmentConfig.apiBaseUrl.value.toHttpUrlOrNull() ?: return chain.proceed(chain.request())
        val original = chain.request()
        val newUrl =
            original.url
                .newBuilder()
                .scheme(current.scheme)
                .host(current.host)
                .port(current.port)
                .build()
        return chain.proceed(original.newBuilder().url(newUrl).build())
    }
}
