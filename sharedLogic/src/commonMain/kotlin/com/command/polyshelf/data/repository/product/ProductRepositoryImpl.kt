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
import com.command.polyshelf.utils.currentTimeMillis
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
        localDataSource.toggleConsumptionLocally(productUuid, !isConsumed)

        val apiResult = if (isConsumed) {
            productApi.unmarkConsumed(productUuid)
        } else {
            productApi.markConsumed(productUuid)
        }

        return if (apiResult.isSuccess) {
            loadInventory().map { }
        } else {
            localDataSource.toggleConsumptionLocally(productUuid, isConsumed)
            Result.failure(
                apiResult.exceptionOrNull() ?: Exception("Failed to toggle consumption")
            )
        }
    }

    override suspend fun deleteProduct(productUuid: String): Result<Unit> {
        localDataSource.deleteProductLocally(productUuid)

        val apiResult = productApi.deleteProduct(productUuid)

        return if (apiResult.isSuccess) {
            loadInventory().map { }
        } else {
            loadInventory().map { }
            Result.failure(
                apiResult.exceptionOrNull() ?: Exception("Failed to delete product")
            )
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
        if (expirationDateStr.isBlank()) return FreshnessStatus.FRESH
        return try {
            val dateComponents = parseDateComponents(expirationDateStr) ?: return FreshnessStatus.FRESH
            val (year, month, day) = dateComponents

            val expEpochDays = toEpochDays(year, month, day)
            val currentEpochDays = currentTimeMillis() / MILLIS_PER_DAY

            determineFreshnessStatus(expEpochDays, currentEpochDays)
        } catch (_: Exception) {
            FreshnessStatus.FRESH
        }
    }

    private fun parseDateComponents(expirationDateStr: String): DateComponents? {
        val datePart = expirationDateStr.trim().split("T").firstOrNull() ?: expirationDateStr
        val parts = datePart.split("-")
        if (parts.size < 3) return null

        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].substring(0, 2).toIntOrNull() ?: return null

        return DateComponents(year, month, day)
    }

    private fun determineFreshnessStatus(expEpochDays: Long, currentEpochDays: Long): FreshnessStatus {
        val daysRemaining = expEpochDays - currentEpochDays
        return when {
            daysRemaining < 0 -> FreshnessStatus.EXPIRED
            daysRemaining <= EXPIRING_SOON_THRESHOLD_DAYS -> FreshnessStatus.EXPIRING_SOON
            else -> FreshnessStatus.FRESH
        }
    }

    private fun toEpochDays(year: Int, month: Int, day: Int): Long {
        val (adjustedYear, adjustedMonth) = adjustYearAndMonth(year, month)
        val era = (if (adjustedYear >= 0) adjustedYear else adjustedYear - 399) / 400
        val yoe = adjustedYear - era * 400
        val doy = (153 * (adjustedMonth - 3) + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    private fun adjustYearAndMonth(year: Int, month: Int): Pair<Long, Long> {
        var y = year.toLong()
        var m = month.toLong()
        if (m <= 2) {
            y -= 1
            m += 12
        }
        return Pair(y, m)
    }

    private data class DateComponents(val year: Int, val month: Int, val day: Int)

    private companion object {
        const val EXPIRING_SOON_THRESHOLD_DAYS = 3L
        const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
}
