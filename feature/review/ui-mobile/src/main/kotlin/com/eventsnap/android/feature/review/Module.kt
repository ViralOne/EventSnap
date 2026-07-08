package com.eventsnap.android.feature.review

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val reviewModule =
    module {
        viewModelOf(::ReviewViewModel)
    }
