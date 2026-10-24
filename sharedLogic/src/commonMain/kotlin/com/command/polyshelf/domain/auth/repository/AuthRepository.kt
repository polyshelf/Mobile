package com.command.polyshelf.domain.auth.repository

import com.command.polyshelf.domain.auth.model.User

interface AuthRepository {
    suspend fun login(user: User): Result<Unit>
    suspend fun register(user: User): Result<Unit>
    suspend fun loginWithYandex(yandexToken: String, linkPassword: String? = null): Result<Unit>
    suspend fun refreshToken(): Result<Unit>
    suspend fun hasToken(): Boolean
    suspend fun logout()
}
