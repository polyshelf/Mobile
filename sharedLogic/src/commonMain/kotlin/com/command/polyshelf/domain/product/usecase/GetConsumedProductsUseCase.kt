package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetConsumedProductsUseCase(
    private val productRepository: ProductRepository
) {
    val consumedProductsFlow: Flow<List<InventoryItem>>
        get() = productRepository.inventoryFlow.map { list ->
            list.filter { it.isConsumed }
        }
}
