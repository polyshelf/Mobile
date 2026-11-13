package com.command.polyshelf.domain.product.repository

import com.command.polyshelf.domain.product.model.InventoryItem
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getInventoryFlow(): Flow<List<InventoryItem>>
    suspend fun loadInventory(): Result<List<InventoryItem>>
    suspend fun addDataMatrixProduct(code: String): Result<Unit>
    suspend fun addBarcodeProduct(code: String, expirationDate: String): Result<Unit>
    suspend fun addBarcodeCandidateProduct(code: String, name: String, expirationDate: String): Result<Unit>
    suspend fun addProductWithoutCode(name: String, expirationDate: String): Result<Unit>
    suspend fun toggleConsumption(productUuid: String, isConsumed: Boolean): Result<Unit>
    suspend fun deleteProduct(productUuid: String): Result<Unit>
}
