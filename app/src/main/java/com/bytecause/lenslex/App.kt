package com.bytecause.lenslex

import android.app.Application
import com.bytecause.lenslex.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            //androidLogger(Level.ERROR) // Use Level.NONE for no logging
            androidContext(this@App)
            modules(appModule)
        }
    }
}