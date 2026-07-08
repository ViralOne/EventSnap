package com.eventsnap.android.core.data.di

import androidx.room.Room
import com.eventsnap.android.core.data.BuildConfig
import com.eventsnap.android.core.data.calendar.CalendarWriter
import com.eventsnap.android.core.data.calendar.CalendarWriterImpl
import com.eventsnap.android.core.data.env.EnvironmentConfig
import com.eventsnap.android.core.data.groq.GroqApi
import com.eventsnap.android.core.data.handoff.ExtractedEventsHolder
import com.eventsnap.android.core.data.history.EventSnapDatabase
import com.eventsnap.android.core.data.network.EnvironmentBaseUrlInterceptor
import com.eventsnap.android.core.data.settings.EncryptedSettingsStore
import com.eventsnap.android.core.data.settings.SettingsStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Wires the shared infrastructure: OkHttp/Retrofit → Groq, the encrypted settings store,
 * the CalendarProvider writer, and the Room history database. Feature data modules depend
 * on these singletons.
 */
val coreDataModule =
    module {
        single { EnvironmentBaseUrlInterceptor(get<EnvironmentConfig>()) }

        single {
            val logging =
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.IS_QA) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            OkHttpClient
                .Builder()
                .addInterceptor(get<EnvironmentBaseUrlInterceptor>())
                .addInterceptor(logging)
                .build()
        }

        single<Moshi> {
            Moshi
                .Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
        }

        single {
            Retrofit
                .Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(get())
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }

        single<GroqApi> { get<Retrofit>().create(GroqApi::class.java) }

        single<SettingsStore> { EncryptedSettingsStore(androidContext()) }

        single<CalendarWriter> { CalendarWriterImpl(androidContext()) }

        single {
            Room
                .databaseBuilder(androidContext(), EventSnapDatabase::class.java, "eventsnap.db")
                .build()
        }
        single { get<EventSnapDatabase>().eventHistoryDao() }

        single { ExtractedEventsHolder() }
    }
