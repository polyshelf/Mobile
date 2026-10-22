package com.command.polyshelf.data.network.auth.exception

sealed class AuthNetworkException : Exception() {
    class InvalidCredentials : AuthNetworkException()
    class UserWithThisEmailAlreadyExists : AuthNetworkException()
    class InvalidEmailOrPasswordFormat : AuthNetworkException()
    class InvalidRefreshToken : AuthNetworkException()
    class ServerError : AuthNetworkException()
    class Unknown(override val message: String? = null) : AuthNetworkException()
}
