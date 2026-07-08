package com.eventsnap.android.feature.capture

import com.eventsnap.android.feature.capture.data.di.captureDataModule
import org.koin.core.module.Module

val captureModules: List<Module> = listOf(captureModule, captureDataModule)
