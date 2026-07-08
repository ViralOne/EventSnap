package com.eventsnap.android.feature.capture

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val captureModule =
    module {
        viewModelOf(::CaptureViewModel)
    }
