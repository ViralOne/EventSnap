package com.eventsnap.android.feature.history.data.di

import com.eventsnap.android.feature.history.data.HistoryRepository
import com.eventsnap.android.feature.history.data.HistoryRepositoryImpl
import org.koin.dsl.module

val historyDataModule =
    module {
        single<HistoryRepository> { HistoryRepositoryImpl(dao = get(), calendarWriter = get()) }
    }
