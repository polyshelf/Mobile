package com.command.polyshelf.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.command.polyshelf.data.storage.DATA_STORE_FILE_NAME
import com.command.polyshelf.data.storage.createDataStore
import com.command.polyshelf.presentation.screen.auth.login.LoginViewModel
import com.command.polyshelf.presentation.screen.auth.registration.RegistrationViewModel
import com.command.polyshelf.presentation.screen.splash.SplashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single<DataStore<Preferences>> {
        createDataStore(
            producePath = { androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
        )
    }
    viewModelOf(::SplashViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegistrationViewModel)
}
