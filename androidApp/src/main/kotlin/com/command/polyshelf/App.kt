package com.command.polyshelf

import android.app.Application
import com.command.polyshelf.di.auth.sharedAuthModule
import com.command.polyshelf.di.authModule
import com.command.polyshelf.di.clientModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        Timber.d("App initialized. Planting Timber DebugTree.")

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(clientModule, sharedAuthModule, authModule)
        }
    }
}
