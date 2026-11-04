package com.command.polyshelf.data.network.product.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductBarcodeCandidateRequest(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("expiration_date") val expirationDate: String
)
