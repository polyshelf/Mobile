package com.command.polyshelf.data.network.product.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDataMatrixRequest(
    @SerialName("code") val code: String
)
