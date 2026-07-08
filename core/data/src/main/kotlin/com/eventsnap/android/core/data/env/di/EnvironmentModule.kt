package com.eventsnap.android.core.data.env.di

import com.eventsnap.android.core.data.env.EnvironmentConfig
import com.eventsnap.android.core.data.env.EnvironmentConfigImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val environmentModule =
    module {
        single<EnvironmentConfig> {
            EnvironmentConfigImpl(
                context = get(),
                scope = CoroutineScope(SupervisorJob()),
                // Both flavors point at Groq's OpenAI-compatible endpoint (see gradle.properties).
                stagingUrl = "https://api.groq.com/openai/v1/",
                prodUrl = "https://api.groq.com/openai/v1/",
            )
        }
    }
