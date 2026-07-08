package com.eventsnap.android.feature.settings.data.di

import com.eventsnap.android.feature.settings.data.SettingsRepository
import com.eventsnap.android.feature.settings.data.SettingsRepositoryImpl
import org.koin.dsl.module

val settingsDataModule =
    module {
        single<SettingsRepository> { SettingsRepositoryImpl(settingsStore = get(), calendarWriter = get()) }
    }
