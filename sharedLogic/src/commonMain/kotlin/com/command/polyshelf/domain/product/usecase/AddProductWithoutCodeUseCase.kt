package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class AddProductWithoutCodeUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(name: String, expirationDate: String): Result<Unit> {
        return productRepository.addProductWithoutCode(name, expirationDate)
    }
}
