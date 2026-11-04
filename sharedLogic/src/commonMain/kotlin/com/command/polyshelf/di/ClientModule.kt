package com.command.polyshelf.di

import com.command.polyshelf.data.network.auth.dto.AuthResponse
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.storage.TokenStorage
import io.ktor.client.HttpClient
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
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val clientModule = module {
    single {
        val tokenStorage: TokenStorage = get()
        HttpClient {
            defaultRequest {
                url("https://polyshelf.wedwin.ru/")
            }
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenStorage.getAccessToken()
                        val refreshToken = tokenStorage.getRefreshToken()
                        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
                            BearerTokens(accessToken, refreshToken)
                        } else null
                    }
                    refreshTokens {
                        val currentRefreshToken = tokenStorage.getRefreshToken()
                        if (currentRefreshToken.isNullOrBlank()) return@refreshTokens null

                        try {
                            val refreshClient = HttpClient {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                            }
                            val response: AuthResponse = refreshClient.post("https://polyshelf.wedwin.ru/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshTokenRequest(refreshToken = currentRefreshToken))
                            }.body()

                            tokenStorage.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (_: Exception) {
                            tokenStorage.clear()
                            null
                        }
                    }
                }
            }
        }
    }
}
