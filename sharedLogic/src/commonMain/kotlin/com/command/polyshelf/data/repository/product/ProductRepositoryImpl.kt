package com.command.polyshelf.data.repository.product

import com.command.polyshelf.data.local.product.ProductLocalDataSource
import com.command.polyshelf.data.local.product.entity.ProductEntity
import com.command.polyshelf.data.network.product.api.ProductApi
import com.command.polyshelf.data.network.product.dto.InventoryItemResponse
import com.command.polyshelf.data.network.product.dto.ProductBarcodeCandidateRequest
import com.command.polyshelf.data.network.product.dto.ProductBarcodeRequest
import com.command.polyshelf.data.network.product.dto.ProductDataMatrixRequest
import com.command.polyshelf.data.network.product.dto.ProductWithoutCodeRequest
import com.command.polyshelf.domain.product.model.FreshnessStatus
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productApi: ProductApi,
    private val localDataSource: ProductLocalDataSource
) : ProductRepository {

    override val inventoryFlow: Flow<List<InventoryItem>> = localDataSource.productsFlow.map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun loadInventory(): Result<List<InventoryItem>> {
        return productApi.getInventory().map { list ->
            val entities = list.map { it.toEntity() }
            localDataSource.saveProducts(entities)
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addDataMatrixProduct(code: String): Result<Unit> {
        return productApi.addDataMatrixProduct(ProductDataMatrixRequest(code)).map {
            loadInventory()
        }
    }

    override suspend fun addBarcodeProduct(code: String, expirationDate: String): Result<Unit> {
        return productApi.addBarcodeProduct(ProductBarcodeRequest(code, expirationDate)).map {
            loadInventory()
        }
    }

    override suspend fun addBarcodeCandidateProduct(
        code: String,
        name: String,
        expirationDate: String
    ): Result<Unit> {
        return productApi.addBarcodeCandidateProduct(
            ProductBarcodeCandidateRequest(code, name, expirationDate)
        ).map {
            loadInventory()
        }
    }

    override suspend fun addProductWithoutCode(
        name: String,
        expirationDate: String
    ): Result<Unit> {
        return productApi.addProductWithoutCode(
            ProductWithoutCodeRequest(name, expirationDate)
        ).map {
            loadInventory()
        }
    }

    override suspend fun toggleConsumption(
        productUuid: String,
        isConsumed: Boolean
    ): Result<Unit> {
        // Optimistic UI update
        localDataSource.toggleConsumptionLocally(productUuid, !isConsumed)

        val apiResult = if (isConsumed) {
            productApi.unmarkConsumed(productUuid)
        } else {
            productApi.markConsumed(productUuid)
        }

        return if (apiResult.isSuccess) {
            loadInventory().map { }
        } else {
            // Revert optimistic update on failure
            localDataSource.toggleConsumptionLocally(productUuid, isConsumed)
            Result.failure(apiResult.exceptionOrNull() ?: Exception("Failed to toggle consumption"))
        }
    }

    override suspend fun deleteProduct(productUuid: String): Result<Unit> {
        // Optimistic UI removal
        localDataSource.deleteProductLocally(productUuid)

        val apiResult = productApi.deleteProduct(productUuid)

        return if (apiResult.isSuccess) {
            loadInventory().map { }
        } else {
            // Restore from server on failure
            loadInventory().map { }
            Result.failure(apiResult.exceptionOrNull() ?: Exception("Failed to delete product"))
        }
    }

    private fun InventoryItemResponse.toEntity(): ProductEntity {
        return ProductEntity(
            id = id,
            name = name,
            expirationDate = expirationDate,
            consumedAt = consumedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun ProductEntity.toDomain(): InventoryItem {
        val status = calculateFreshness(this.expirationDate)
        return InventoryItem(
            id = id,
            name = name,
            expirationDate = expirationDate,
            isConsumed = !consumedAt.isNullOrBlank(),
            freshnessStatus = status
        )
    }

    private fun calculateFreshness(expirationDateStr: String): FreshnessStatus {
        return try {
            FreshnessStatus.FRESH
        } catch (_: Exception) {
            FreshnessStatus.FRESH
        }
    }
}
