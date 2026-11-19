package com.command.polyshelf.domain.auth.usecase

import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.utils.Logger

class CheckAuthUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        Logger.d("CheckAuthUseCase", "Checking if stored tokens exist...")
        if (!authRepository.hasToken()) {
            Logger.d("CheckAuthUseCase", "No tokens found in storage.")
            return Result.failure(AuthNetworkException.InvalidRefreshToken())
        }
        Logger.d("CheckAuthUseCase", "Tokens found. Invoking refreshToken()...")
        return authRepository.refreshToken()
    }

    suspend fun logout() {
        Logger.d("CheckAuthUseCase", "logout() called")
        authRepository.logout()
    }
}
