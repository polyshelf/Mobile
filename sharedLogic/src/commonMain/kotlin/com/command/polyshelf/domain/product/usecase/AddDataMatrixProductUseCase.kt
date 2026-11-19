package com.command.polyshelf.domain.product.usecase

import com.command.polyshelf.domain.product.repository.ProductRepository

class AddDataMatrixProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(code: String): Result<Unit> {
        return productRepository.addDataMatrixProduct(code)
    }
}
