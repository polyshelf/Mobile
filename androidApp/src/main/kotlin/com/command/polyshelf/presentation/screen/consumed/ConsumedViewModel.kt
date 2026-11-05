package com.command.polyshelf.presentation.screen.consumed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.usecase.DeleteProductUseCase
import com.command.polyshelf.domain.product.usecase.GetConsumedProductsUseCase
import com.command.polyshelf.domain.product.usecase.ToggleConsumptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ConsumedViewModel(
    private val getConsumedProductsUseCase: GetConsumedProductsUseCase,
    private val toggleConsumptionUseCase: ToggleConsumptionUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    val state: StateFlow<ConsumedState>
        field = MutableStateFlow(ConsumedState())

    init {
        viewModelScope.launch {
            getConsumedProductsUseCase.consumedProductsFlow.collect { items ->
                state.update { it.copy(items = items) }
            }
        }
    }

    fun onIntent(intent: ConsumedIntent) {
        when (intent) {
            is ConsumedIntent.UnmarkConsumed -> handleUnmarkConsumed(intent.item)
            is ConsumedIntent.DeleteProduct -> handleDeleteProduct(intent.productUuid)
            is ConsumedIntent.ClearError -> state.update { it.copy(networkErrorMessage = null) }
        }
    }

    private fun handleUnmarkConsumed(item: InventoryItem) {
        viewModelScope.launch {
            val result = toggleConsumptionUseCase(item.id, item.isConsumed)
            if (result.isFailure) {
                Timber.e(result.exceptionOrNull(), "Failed to unmark consumed")
                state.update { it.copy(networkErrorMessage = "Не удалось изменить статус продукта") }
            }
        }
    }

    private fun handleDeleteProduct(productUuid: String) {
        viewModelScope.launch {
            val result = deleteProductUseCase(productUuid)
            if (result.isFailure) {
                Timber.e(result.exceptionOrNull(), "Failed to delete product")
                state.update { it.copy(networkErrorMessage = "Не удалось удалить продукт") }
            }
        }
    }
}

data class ConsumedState(
    val items: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val networkErrorMessage: String? = null
)

sealed interface ConsumedIntent {
    data class UnmarkConsumed(val item: InventoryItem) : ConsumedIntent
    data class DeleteProduct(val productUuid: String) : ConsumedIntent
    data object ClearError : ConsumedIntent
}
