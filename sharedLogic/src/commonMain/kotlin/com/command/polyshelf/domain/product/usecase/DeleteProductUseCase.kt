package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class DeleteProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productUuid: String): Result<Unit> {
        return productRepository.deleteProduct(productUuid)
    }
}
