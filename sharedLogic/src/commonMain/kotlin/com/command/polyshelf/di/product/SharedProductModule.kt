package com.command.polyshelf.di.product

import com.command.polyshelf.data.local.product.ProductLocalDataSource
import com.command.polyshelf.data.network.product.api.ProductApi
import com.command.polyshelf.data.network.product.api.impl.ProductApiImpl
import com.command.polyshelf.data.repository.product.ProductRepositoryImpl
import com.command.polyshelf.domain.product.repository.ProductRepository
import com.command.polyshelf.domain.product.usecase.AddBarcodeCandidateProductUseCase
import com.command.polyshelf.domain.product.usecase.AddBarcodeProductUseCase
import com.command.polyshelf.domain.product.usecase.AddDataMatrixProductUseCase
import com.command.polyshelf.domain.product.usecase.AddProductWithoutCodeUseCase
import com.command.polyshelf.domain.product.usecase.DeleteProductUseCase
import com.command.polyshelf.domain.product.usecase.GetConsumedProductsUseCase
import com.command.polyshelf.domain.product.usecase.GetInventoryUseCase
import com.command.polyshelf.domain.product.usecase.ToggleConsumptionUseCase
import org.koin.dsl.module

val sharedProductModule = module {
    single { ProductLocalDataSource() }
    single<ProductApi> { ProductApiImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get(), get()) }
    single { GetInventoryUseCase(get()) }
    single { GetConsumedProductsUseCase(get()) }
    single { AddDataMatrixProductUseCase(get()) }
    single { AddBarcodeProductUseCase(get()) }
    single { AddBarcodeCandidateProductUseCase(get()) }
    single { AddProductWithoutCodeUseCase(get()) }
    single { ToggleConsumptionUseCase(get()) }
    single { DeleteProductUseCase(get()) }
}
