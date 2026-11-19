package com.command.polyshelf.data.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String = ""
)
