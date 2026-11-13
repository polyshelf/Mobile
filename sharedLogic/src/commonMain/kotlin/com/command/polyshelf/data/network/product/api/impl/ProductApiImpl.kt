package com.command.polyshelf.data.network.product.api.impl

import com.command.polyshelf.data.network.product.api.ProductApi
import com.command.polyshelf.data.network.product.dto.InventoryItemResponse
import com.command.polyshelf.data.network.product.dto.ProductBarcodeCandidateRequest
import com.command.polyshelf.data.network.product.dto.ProductBarcodeRequest
import com.command.polyshelf.data.network.product.dto.ProductDataMatrixRequest
import com.command.polyshelf.data.network.product.dto.ProductResponse
import com.command.polyshelf.data.network.product.dto.ProductWithoutCodeRequest
import com.command.polyshelf.data.network.product.exception.ProductNetworkException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.CancellationException
import kotlinx.io.IOException

class ProductApiImpl(
    private val client: HttpClient
) : ProductApi {

    override suspend fun addDataMatrixProduct(request: ProductDataMatrixRequest): Result<ProductResponse> =
        safeCall {
            client.post("products/from-datamatrix") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }

    override suspend fun addBarcodeProduct(request: ProductBarcodeRequest): Result<ProductResponse> =
        safeCall {
            client.post("products/from-barcode") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }

    override suspend fun addBarcodeCandidateProduct(request: ProductBarcodeCandidateRequest): Result<ProductResponse> =
        safeCall {
            client.post("products/from-barcode-candidate") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }

    override suspend fun addProductWithoutCode(request: ProductWithoutCodeRequest): Result<ProductResponse> =
        safeCall {
            client.post("products") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }

    override suspend fun getInventory(): Result<List<InventoryItemResponse>> = safeCall {
        client.get("products/inventory").body()
    }

    override suspend fun markConsumed(productUuid: String): Result<Unit> = safeCall {
        client.put("products/$productUuid/consumption")
    }

    override suspend fun unmarkConsumed(productUuid: String): Result<Unit> = safeCall {
        client.delete("products/$productUuid/consumption")
    }

    override suspend fun deleteProduct(productUuid: String): Result<Unit> = safeCall {
        client.delete("products/$productUuid")
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.NotFound -> Result.failure(ProductNetworkException.ProductNotFound())
                else -> Result.failure(ProductNetworkException.Unknown(e.message))
            }
        } catch (_: ServerResponseException) {
            Result.failure(ProductNetworkException.Unknown("Ошибка сервера"))
        } catch (_: IOException) {
            Result.failure(ProductNetworkException.NetworkError())
        } catch (e: Exception) {
            Result.failure(ProductNetworkException.Unknown(e.message))
        }
    }
}
