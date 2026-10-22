package com.command.polyshelf.data.network.auth.api.impl

import com.command.polyshelf.data.network.auth.api.AuthApi
import com.command.polyshelf.data.network.auth.dto.AuthResponse
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.network.auth.dto.UserDto
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.CancellationException

class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {

    override suspend fun login(user: UserDto): Result<AuthResponse> {
        return try {
            val response = client.post(END_POINT_LOGIN) {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            val authResponse = response.body<AuthResponse>()
            Result.success(authResponse)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(AuthNetworkException.InvalidCredentials())
                HttpStatusCode.BadRequest -> Result.failure(AuthNetworkException.InvalidEmailOrPasswordFormat())
                else -> Result.failure(AuthNetworkException.Unknown(e.message))
            }
        } catch (_: ServerResponseException) {
            Result.failure(AuthNetworkException.ServerError())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(user: UserDto): Result<AuthResponse> {
        return try {
            val response = client.post(END_POINT_REGISTER) {
                contentType(ContentType.Application.Json)
                setBody(user)
            }
            val authResponse = response.body<AuthResponse>()
            Result.success(authResponse)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Conflict -> Result.failure(AuthNetworkException.UserWithThisEmailAlreadyExists())
                HttpStatusCode.BadRequest -> Result.failure(AuthNetworkException.InvalidEmailOrPasswordFormat())
                else -> Result.failure(AuthNetworkException.Unknown(e.message))
            }
        } catch (_: ServerResponseException) {
            Result.failure(AuthNetworkException.ServerError())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refresh(request: RefreshTokenRequest): Result<AuthResponse> {
        return try {
            val response = client.post(END_POINT_REFRESH) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val authResponse = response.body<AuthResponse>()
            Result.success(authResponse)
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Unauthorized -> Result.failure(AuthNetworkException.InvalidRefreshToken())
                else -> Result.failure(AuthNetworkException.Unknown(e.message))
            }
        } catch (_: ServerResponseException) {
            Result.failure(AuthNetworkException.ServerError())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val END_POINT_LOGIN = "auth/login"
        const val END_POINT_REGISTER = "auth/register"
        const val END_POINT_REFRESH = "auth/refresh"
    }
}
