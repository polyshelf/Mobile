package com.command.polyshelf.domain.product.model

data class InventoryItem(
    val id: String,
    val name: String,
    val expirationDate: String,
    val isConsumed: Boolean,
    val freshnessStatus: FreshnessStatus
)

enum class FreshnessStatus {
    FRESH,
    EXPIRING_SOON,
    EXPIRED
}
