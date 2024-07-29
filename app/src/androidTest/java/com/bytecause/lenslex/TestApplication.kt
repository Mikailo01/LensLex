package com.bytecause.lenslex

import android.app.Application
import com.bytecause.lenslex.di.appModule
import com.bytecause.lenslex.di.testModule
import org.koin.core.context.startKoin

class TestApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule, testModule)
        }
    }
}