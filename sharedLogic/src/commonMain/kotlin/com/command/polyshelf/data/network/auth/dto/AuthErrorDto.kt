package com.command.polyshelf.data.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthErrorDto(
    @SerialName("code") val code: String? = null,
    @SerialName("message") val message: String? = null
)
