package com.command.polyshelf.data.repository.auth

import com.command.polyshelf.data.network.auth.api.AuthApi
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.data.storage.TokenStorage
import com.command.polyshelf.domain.auth.mapper.toDto
import com.command.polyshelf.domain.auth.model.User
import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.utils.Logger

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(user: User): Result<Unit> {
        Logger.d("AuthRepositoryImpl", "login() started for email=${user.email}")
        return authApi.login(user.toDto()).mapCatching { response ->
            Logger.d("AuthRepositoryImpl", "login() SUCCESS -> saving tokens")
            tokenStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
        }.onFailure { ex ->
            Logger.e("AuthRepositoryImpl", "login() FAILED: ${ex.message}", ex)
        }
    }

    override suspend fun register(user: User): Result<Unit> {
        Logger.d("AuthRepositoryImpl", "register() started for email=${user.email}")
        return authApi.register(user.toDto()).mapCatching { response ->
            Logger.d("AuthRepositoryImpl", "register() SUCCESS -> saving tokens")
            tokenStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
        }.onFailure { ex ->
            Logger.e("AuthRepositoryImpl", "register() FAILED: ${ex.message}", ex)
        }
    }

    override suspend fun hasToken(): Boolean {
        val refreshToken = tokenStorage.getRefreshToken()
        val hasTokens = !refreshToken.isNullOrBlank()
        Logger.d("AuthRepositoryImpl", "hasTokens = $hasTokens")
        return hasTokens
    }

    override suspend fun refreshToken(): Result<Unit> {
        val currentRefreshToken = tokenStorage.getRefreshToken()
        Logger.d("AuthRepositoryImpl", "refreshToken() started. currentRefreshToken is ${if (currentRefreshToken.isNullOrBlank()) "NULL/EMPTY" else "PRESENT"}")

        if (currentRefreshToken.isNullOrBlank()) {
            Logger.e("AuthRepositoryImpl", "Cannot refresh token: refreshToken is null or blank")
            return Result.failure(AuthNetworkException.InvalidRefreshToken())
        }

        val result = authApi.refresh(RefreshTokenRequest(refreshToken = currentRefreshToken))

        result.onSuccess { response ->
            Logger.d("AuthRepositoryImpl", "authApi.refresh SUCCESS. Saving new tokens...")
            tokenStorage.saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken
            )
        }.onFailure { ex ->
            Logger.e("AuthRepositoryImpl", "authApi.refresh FAILED: ${ex.message}", ex)
            if (ex is AuthNetworkException.InvalidRefreshToken) {
                Logger.d("AuthRepositoryImpl", "Refresh token is invalid/expired (401). Clearing stored tokens.")
                tokenStorage.clear()
            }
        }

        return result.map { }
    }

    override suspend fun logout() {
        Logger.d("AuthRepositoryImpl", "logout() called -> clearing tokenStorage")
        tokenStorage.clear()
    }
}
