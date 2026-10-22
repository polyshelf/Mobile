package com.command.polyshelf.di.auth

import com.command.polyshelf.data.network.auth.api.AuthApi
import com.command.polyshelf.data.network.auth.api.impl.AuthApiImpl
import com.command.polyshelf.data.repository.auth.AuthRepositoryImpl
import com.command.polyshelf.data.storage.DataStoreTokenStorage
import com.command.polyshelf.data.storage.TokenStorage
import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.domain.auth.usecase.CheckAuthUseCase
import com.command.polyshelf.domain.auth.usecase.LoginUseCase
import com.command.polyshelf.domain.auth.usecase.RegisterUseCase
import org.koin.dsl.module

val sharedAuthModule = module {
    single<TokenStorage> { DataStoreTokenStorage(get()) }
    single<AuthApi> { AuthApiImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<LoginUseCase> { LoginUseCase(get()) }
    single<RegisterUseCase> { RegisterUseCase(get()) }
    single<CheckAuthUseCase> { CheckAuthUseCase(get()) }
}
