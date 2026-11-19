package com.command.polyshelf.data.network.product.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ProductResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @JsonNames("expirationDate", "expiration_date") val expirationDate: String
)
