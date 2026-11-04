package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class AddBarcodeProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(code: String, expirationDate: String): Result<Unit> {
        return productRepository.addBarcodeProduct(code, expirationDate)
    }
}
