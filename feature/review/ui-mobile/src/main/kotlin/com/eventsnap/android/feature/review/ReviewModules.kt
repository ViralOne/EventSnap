package com.eventsnap.android.feature.review

import com.eventsnap.android.feature.review.data.di.reviewDataModule
import org.koin.core.module.Module

val reviewModules: List<Module> = listOf(reviewModule, reviewDataModule)
