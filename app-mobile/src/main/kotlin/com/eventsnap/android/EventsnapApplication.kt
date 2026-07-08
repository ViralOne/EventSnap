package com.eventsnap.android

import android.app.Application
import com.eventsnap.android.core.data.di.coreDataModule
import com.eventsnap.android.core.data.env.di.environmentModule
import com.eventsnap.android.feature.capture.captureModules
import com.eventsnap.android.feature.history.historyModules
import com.eventsnap.android.feature.review.reviewModules
import com.eventsnap.android.feature.settings.settingsModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class EventsnapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        startKoin {
            androidContext(this@EventsnapApplication)
            modules(
                listOf(environmentModule, coreDataModule) +
                    captureModules +
                    reviewModules +
                    settingsModules +
                    historyModules,
            )
        }
    }
}
