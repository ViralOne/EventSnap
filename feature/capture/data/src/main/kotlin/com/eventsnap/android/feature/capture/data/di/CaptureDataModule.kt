package com.eventsnap.android.feature.capture.data.di

import com.eventsnap.android.feature.capture.data.CaptureRepository
import com.eventsnap.android.feature.capture.data.CaptureRepositoryImpl
import org.koin.dsl.module

val captureDataModule =
    module {
        single<CaptureRepository> { CaptureRepositoryImpl(groqApi = get(), settingsStore = get(), moshi = get()) }
    }
