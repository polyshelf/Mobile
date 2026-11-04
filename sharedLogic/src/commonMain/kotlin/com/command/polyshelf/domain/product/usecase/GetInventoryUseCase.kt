package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetInventoryUseCase(
    private val productRepository: ProductRepository
) {
    val inventoryFlow: Flow<List<InventoryItem>>
        get() = productRepository.inventoryFlow.map { list ->
            list.filter { !it.isConsumed }
        }

    suspend fun refresh(): Result<List<InventoryItem>> {
        return productRepository.loadInventory()
    }
}
