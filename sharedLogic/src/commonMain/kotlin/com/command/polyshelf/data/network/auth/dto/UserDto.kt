package com.command.polyshelf.data.network.auth.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)
