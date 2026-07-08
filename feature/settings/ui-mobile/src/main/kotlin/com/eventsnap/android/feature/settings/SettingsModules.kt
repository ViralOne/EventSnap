package com.eventsnap.android.feature.settings

import com.eventsnap.android.feature.settings.data.di.settingsDataModule
import org.koin.core.module.Module

val settingsModules: List<Module> = listOf(settingsModule, settingsDataModule)
