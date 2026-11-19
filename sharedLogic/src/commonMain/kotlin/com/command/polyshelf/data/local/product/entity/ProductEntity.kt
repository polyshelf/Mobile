package com.command.polyshelf.data.local.product.entity

data class ProductEntity(
    val id: String,
    val name: String,
    val expirationDate: String,
    val consumedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
