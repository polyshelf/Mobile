package com.command.polyshelf.domain.inventory

data class FoodItem(
    val id: Long,
    val productName: String,
    val createdAt: Long,
    val expiresAt: Long
)
