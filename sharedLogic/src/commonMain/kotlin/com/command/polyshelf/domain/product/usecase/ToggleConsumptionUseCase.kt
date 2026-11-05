package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class ToggleConsumptionUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productUuid: String, isConsumed: Boolean): Result<Unit> {
        return productRepository.toggleConsumption(productUuid, isConsumed)
    }
}
