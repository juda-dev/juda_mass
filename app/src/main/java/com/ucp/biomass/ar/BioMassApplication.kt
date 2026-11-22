package com.ucp.biomass.ar

import android.app.Application
import com.ucp.biomass.ar.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BioMassApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@BioMassApplication)
            modules(appModule)
        }
    }
}
