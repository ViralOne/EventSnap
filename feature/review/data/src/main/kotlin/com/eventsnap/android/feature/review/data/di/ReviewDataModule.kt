package com.eventsnap.android.feature.review.data.di

import com.eventsnap.android.feature.review.data.ReviewRepository
import com.eventsnap.android.feature.review.data.ReviewRepositoryImpl
import org.koin.dsl.module

val reviewDataModule =
    module {
        single<ReviewRepository> {
            ReviewRepositoryImpl(
                extractedEventsHolder = get(),
                calendarWriter = get(),
                settingsStore = get(),
                historyDao = get(),
            )
        }
    }
