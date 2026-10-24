package com.command.polyshelf.data.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YandexAuthRequest(
    @SerialName("access_token") val accessToken: String,
    @SerialName("link_password") val linkPassword: String? = null
)
