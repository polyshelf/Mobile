package com.command.polyshelf.di

import com.command.polyshelf.data.network.auth.dto.AuthResponse
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.storage.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private const val BASE_URL = "https://polyshelf.wedwin.ru/"
private const val REFRESH_ENDPOINT = "https://polyshelf.wedwin.ru/auth/refresh"

val clientModule = module {
    single { createHttpClient(get()) }
}

private fun createHttpClient(tokenStorage: TokenStorage): HttpClient {
    val refreshMutex = Mutex()

    return HttpClient {
        defaultRequest {
            url(BASE_URL)
        }
        expectSuccess = true
        configureJsonSerialization()
        configureAuth(tokenStorage, refreshMutex)
    }
}

private fun HttpClientConfig<*>.configureJsonSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
}

private fun HttpClientConfig<*>.configureAuth(
    tokenStorage: TokenStorage,
    refreshMutex: Mutex
) {
    install(Auth) {
        bearer {
            loadTokens {
                fetchStoredBearerTokens(tokenStorage)
            }
            refreshTokens {
                refreshMutex.withLock {
                    handleTokenRefresh(tokenStorage)
                }
            }
        }
    }
}

private suspend fun fetchStoredBearerTokens(tokenStorage: TokenStorage): BearerTokens? {
    val accessToken = tokenStorage.getAccessToken()
    val refreshToken = tokenStorage.getRefreshToken()
    return if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
        BearerTokens(accessToken, refreshToken)
    } else null
}

private suspend fun handleTokenRefresh(tokenStorage: TokenStorage): BearerTokens? {
    val currentRefreshToken = tokenStorage.getRefreshToken()
    if (currentRefreshToken.isNullOrBlank()) return null

    val refreshClient = createRefreshClient()
    return try {
        val response = executeRefreshRequest(refreshClient, currentRefreshToken)
        tokenStorage.saveTokens(response.accessToken, response.refreshToken)
        BearerTokens(response.accessToken, response.refreshToken)
    } catch (_: Exception) {
        tokenStorage.clear()
        null
    } finally {
        refreshClient.close()
    }
}

private fun createRefreshClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}

private suspend fun executeRefreshRequest(
    client: HttpClient,
    refreshToken: String
): AuthResponse {
    return client.post(REFRESH_ENDPOINT) {
        contentType(ContentType.Application.Json)
        setBody(RefreshTokenRequest(refreshToken = refreshToken))
    }.body()
}
