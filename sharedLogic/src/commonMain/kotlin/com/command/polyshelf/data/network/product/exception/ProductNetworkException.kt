package com.command.polyshelf.data.network.product.exception

sealed class ProductNetworkException : Exception() {
    class ProductNotFound : ProductNetworkException()
    class NetworkError(override val message: String? = "Отсутствует подключение к сети") :
        ProductNetworkException()

    class Unknown(override val message: String? = null) : ProductNetworkException()
}
