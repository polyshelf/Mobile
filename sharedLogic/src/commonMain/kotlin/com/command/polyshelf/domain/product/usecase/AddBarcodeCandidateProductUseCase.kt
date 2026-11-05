package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class AddBarcodeCandidateProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(code: String, name: String, expirationDate: String): Result<Unit> {
        return productRepository.addBarcodeCandidateProduct(code, name, expirationDate)
    }
}
