package com.command.polyshelf.domain.auth.usecase

import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.utils.validateAsPassword

class ChangePasswordUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String? = null
    ): Result<Unit> {
        if (currentPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Введите текущий пароль"))
        }
        if (newPassword.isBlank()) {
            return Result.failure(IllegalArgumentException("Введите новый пароль"))
        }
        if (confirmPassword != null && newPassword != confirmPassword) {
            return Result.failure(IllegalArgumentException("Пароли не совпадают"))
        }
        val validNewPassword = newPassword.validateAsPassword().getOrElse {
            return Result.failure(it)
        }
        return authRepository.changePassword(currentPassword, validNewPassword)
    }
}
