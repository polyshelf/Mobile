package com.command.polyshelf.domain.auth.usecase

import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.utils.Logger

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        Logger.d("LogoutUseCase", "logout() called")
        authRepository.logout()
    }
}
