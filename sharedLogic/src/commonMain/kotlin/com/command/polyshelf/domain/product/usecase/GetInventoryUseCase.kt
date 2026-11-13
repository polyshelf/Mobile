package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.repository.ProductRepository
import com.command.polyshelf.utils.parseExpirationDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetInventoryUseCase(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<InventoryItem>> =
        productRepository.getInventoryFlow().map { list ->
            list.filter { !it.isConsumed }
                .sortedWith(compareBy(nullsLast()) { parseExpirationDate(it.expirationDate) })
        }
}
