package com.command.polyshelf.domain.auth.usecase

import com.command.polyshelf.domain.auth.model.User
import com.command.polyshelf.domain.auth.repository.AuthRepository
import com.command.polyshelf.utils.validateAsEmail
import com.command.polyshelf.utils.validateAsPassword

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        val validEmail = email.validateAsEmail()
            .getOrElse {
                return Result.failure(it)
            }

        val validPassword = password.validateAsPassword()
            .getOrElse {
                return Result.failure(it)
            }

        val user = User(validEmail, validPassword)
        return authRepository.login(user)
    }
}
