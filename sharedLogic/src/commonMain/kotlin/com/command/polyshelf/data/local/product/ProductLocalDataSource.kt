package com.command.polyshelf.data.local.product

import com.command.polyshelf.data.local.product.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProductLocalDataSource {
    private val _productsFlow = MutableStateFlow<List<ProductEntity>>(emptyList())
    val productsFlow: Flow<List<ProductEntity>> = _productsFlow.asStateFlow()

    fun saveProducts(products: List<ProductEntity>) {
        _productsFlow.value = products
    }

    fun deleteProductLocally(id: String) {
        _productsFlow.value = _productsFlow.value.filter { it.id != id }
    }

    fun toggleConsumptionLocally(id: String, isConsumed: Boolean) {
        val nowStr = if (isConsumed) "consumed" else null
        _productsFlow.value = _productsFlow.value.map { entity ->
            if (entity.id == id) {
                entity.copy(consumedAt = nowStr)
            } else {
                entity
            }
        }
    }

    fun clear() {
        _productsFlow.value = emptyList()
    }
}
