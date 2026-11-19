package com.command.polyshelf.data.network.auth.api.impl

import com.command.polyshelf.data.network.auth.api.AuthApi
import com.command.polyshelf.data.network.auth.dto.AuthErrorDto
import com.command.polyshelf.data.network.auth.dto.AuthResponse
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.network.auth.dto.UserDto
import com.command.polyshelf.data.network.auth.dto.YandexAuthRequest
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
                HttpStatusCode.Conflict -> Result.failure(e.toConflictException())
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
                HttpStatusCode.Conflict -> Result.failure(e.toConflictException())
                HttpStatusCode.BadRequest -> Result.failure(AuthNetworkException.InvalidEmailOrPasswordFormat())
                else -> Result.failure(AuthNetworkException.Unknown(e.message))
            }
        } catch (_: ServerResponseException) {
            Result.failure(AuthNetworkException.ServerError())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun yandex(request: YandexAuthRequest): Result<AuthResponse> {
        return try {
            val response = client.post(END_POINT_YANDEX) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Result.success(response.body<AuthResponse>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            val error = e.readError()
            when {
                error?.code == CODE_YANDEX_LINK_REQUIRED ->
                    Result.failure(AuthNetworkException.YandexLinkRequired(error.message))
                error?.code == CODE_INVALID_PASSWORD ->
                    Result.failure(AuthNetworkException.InvalidLinkPassword())
                error?.code == CODE_YANDEX_TOKEN_INVALID ->
                    Result.failure(AuthNetworkException.YandexTokenInvalid())
                e.response.status == HttpStatusCode.Conflict -> Result.failure(e.toConflictException())
                else -> Result.failure(AuthNetworkException.Unknown(error?.message ?: e.message))
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

    private suspend fun ClientRequestException.readError(): AuthErrorDto? = try {
        response.body<AuthErrorDto>()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private suspend fun ClientRequestException.toConflictException(): AuthNetworkException =
        when (readError()?.code) {
            CODE_EMAIL_BOUND_TO_YANDEX -> AuthNetworkException.EmailBoundToYandex()
            else -> AuthNetworkException.UserWithThisEmailAlreadyExists()
        }

    private companion object {
        const val END_POINT_YANDEX = "auth/yandex"
        const val CODE_EMAIL_BOUND_TO_YANDEX = "EMAIL_BOUND_TO_YANDEX"
        const val CODE_YANDEX_LINK_REQUIRED = "YANDEX_LINK_REQUIRED"
        const val CODE_INVALID_PASSWORD = "INVALID_PASSWORD"
        const val CODE_YANDEX_TOKEN_INVALID = "YANDEX_TOKEN_INVALID"
        const val END_POINT_LOGIN = "auth/login"
        const val END_POINT_REGISTER = "auth/register"
        const val END_POINT_REFRESH = "auth/refresh"
    }
}
