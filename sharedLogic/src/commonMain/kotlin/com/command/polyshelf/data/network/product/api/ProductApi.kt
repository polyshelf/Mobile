package com.command.polyshelf.data.network.product.api

import com.command.polyshelf.data.network.product.dto.InventoryItemResponse
import com.command.polyshelf.data.network.product.dto.ProductBarcodeCandidateRequest
import com.command.polyshelf.data.network.product.dto.ProductBarcodeRequest
import com.command.polyshelf.data.network.product.dto.ProductDataMatrixRequest
import com.command.polyshelf.data.network.product.dto.ProductResponse
import com.command.polyshelf.data.network.product.dto.ProductWithoutCodeRequest

interface ProductApi {
    suspend fun addDataMatrixProduct(request: ProductDataMatrixRequest): Result<ProductResponse>
    suspend fun addBarcodeProduct(request: ProductBarcodeRequest): Result<ProductResponse>
    suspend fun addBarcodeCandidateProduct(request: ProductBarcodeCandidateRequest): Result<ProductResponse>
    suspend fun addProductWithoutCode(request: ProductWithoutCodeRequest): Result<ProductResponse>
    suspend fun getInventory(): Result<List<InventoryItemResponse>>
    suspend fun markConsumed(productUuid: String): Result<Unit>
    suspend fun unmarkConsumed(productUuid: String): Result<Unit>
    suspend fun deleteProduct(productUuid: String): Result<Unit>
}
