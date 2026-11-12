package com.command.polyshelf.presentation.screen.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.product.exception.ProductNetworkException
import com.command.polyshelf.domain.product.model.InventoryItem
import com.command.polyshelf.domain.product.usecase.DeleteProductUseCase
import com.command.polyshelf.domain.product.usecase.GetInventoryUseCase
import com.command.polyshelf.domain.product.usecase.RefreshInventoryUseCase
import com.command.polyshelf.domain.product.usecase.ToggleConsumptionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class InventoryViewModel(
    private val getInventoryUseCase: GetInventoryUseCase,
    private val refreshInventoryUseCase: RefreshInventoryUseCase,
    private val toggleConsumptionUseCase: ToggleConsumptionUseCase,
    private val deleteProductUseCase: DeleteProductUseCase
) : ViewModel() {

    val state: StateFlow<InventoryState>
        field = MutableStateFlow(InventoryState())

    init {
        viewModelScope.launch {
            getInventoryUseCase().collect { items ->
                state.update { it.copy(items = items) }
            }
        }
        loadInventory()
    }

    fun onIntent(intent: InventoryIntent) {
        when (intent) {
            is InventoryIntent.LoadInventory -> loadInventory()
            is InventoryIntent.ToggleConsumption -> handleToggleConsumption(intent.item)
            is InventoryIntent.DeleteProduct -> handleDeleteProduct(intent.productUuid)
            is InventoryIntent.ClearError -> state.update { it.copy(networkErrorMessage = null) }
        }
    }

    private fun loadInventory() {
        viewModelScope.launch {
            if (state.value.items.isEmpty()) {
                state.update { it.copy(isLoading = true, networkErrorMessage = null) }
            }

            val result = refreshInventoryUseCase()
            if (result.isFailure) {
                val ex = result.exceptionOrNull()
                Timber.e(ex, "Failed to load inventory")
                val msg = if (ex is ProductNetworkException.NetworkError) {
                    "Отсутствует сеть или плохое соединение."
                } else {
                    ex?.message ?: "Не удалось загрузить список продуктов."
                }
                state.update { it.copy(isLoading = false, networkErrorMessage = msg) }
            } else {
                state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun handleToggleConsumption(item: InventoryItem) {
        viewModelScope.launch {
            val result = toggleConsumptionUseCase(item.id, item.isConsumed)
            if (result.isFailure) {
                val ex = result.exceptionOrNull()
                Timber.e(ex, "Failed to toggle consumption")
                state.update { it.copy(networkErrorMessage = "Не удалось обновить статус продукта") }
            }
        }
    }

    private fun handleDeleteProduct(productUuid: String) {
        viewModelScope.launch {
            val result = deleteProductUseCase(productUuid)
            if (result.isFailure) {
                val ex = result.exceptionOrNull()
                Timber.e(ex, "Failed to delete product")
                state.update { it.copy(networkErrorMessage = "Не удалось удалить продукт") }
            }
        }
    }
}

data class InventoryState(
    val items: List<InventoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val networkErrorMessage: String? = null
)

sealed interface InventoryIntent {
    data object LoadInventory : InventoryIntent
    data class ToggleConsumption(val item: InventoryItem) : InventoryIntent
    data class DeleteProduct(val productUuid: String) : InventoryIntent
    data object ClearError : InventoryIntent
}
