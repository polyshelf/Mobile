package com.command.polyshelf.utils

import com.command.polyshelf.domain.auth.exception.AuthUIException

private val emailRegex =
    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        .toRegex()

internal fun String.isValidEmail() =
    emailRegex.matches(this)

internal fun String.validateAsEmail(): Result<String> {
    val trimmed = this.trim()
    return when {
        trimmed.isBlank() -> Result.failure(AuthUIException.EmptyEmail())
        !trimmed.isValidEmail() -> Result.failure(AuthUIException.InvalidEmailFormat())
        else -> Result.success(trimmed)
    }
}

internal fun String.validateAsPassword(): Result<String> {
    val trimmed = this.trim()
    return if (trimmed.isBlank() || trimmed.length < 6) {
        Result.failure(AuthUIException.InvalidPassword())
    } else {
        Result.success(trimmed)
    }
}
