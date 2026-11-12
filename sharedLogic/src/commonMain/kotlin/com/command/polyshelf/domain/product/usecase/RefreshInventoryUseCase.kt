package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.repository.ProductRepository

class RefreshInventoryUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(): Result<List<InventoryItem>> {
        return productRepository.loadInventory()
    }
}
