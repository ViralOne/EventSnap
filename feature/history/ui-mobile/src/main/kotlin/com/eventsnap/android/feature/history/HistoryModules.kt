package com.eventsnap.android.feature.history

import com.eventsnap.android.feature.history.data.di.historyDataModule
import org.koin.core.module.Module

val historyModules: List<Module> = listOf(historyModule, historyDataModule)
