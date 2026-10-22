package com.command.polyshelf.domain.auth.exception

sealed class AuthUIException : Exception() {
    class EmptyEmail : AuthUIException()
    class InvalidPassword : AuthUIException()
    class InvalidEmailFormat : AuthUIException()
}
