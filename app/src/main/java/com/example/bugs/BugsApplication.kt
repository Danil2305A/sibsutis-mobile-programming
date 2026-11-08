package com.example.bugs

import android.app.Application
import com.example.bugs.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BugsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            // обычно отключают в релизной сборке
            // и присылают логи на Firebase Crashlytics
            androidLogger()

            androidContext(this@BugsApplication)
            modules(appModule)
        }
    }
}