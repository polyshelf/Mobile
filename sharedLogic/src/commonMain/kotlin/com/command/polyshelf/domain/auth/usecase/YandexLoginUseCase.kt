package com.command.polyshelf.domain.auth.usecase

import com.command.polyshelf.domain.auth.repository.AuthRepository

class YandexLoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(yandexToken: String, linkPassword: String? = null): Result<Unit> =
        authRepository.loginWithYandex(yandexToken, linkPassword)
}
