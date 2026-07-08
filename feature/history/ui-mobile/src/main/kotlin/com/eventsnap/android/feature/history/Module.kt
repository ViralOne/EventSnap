package com.eventsnap.android.feature.history

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyModule =
    module {
        viewModelOf(::HistoryViewModel)
    }
