package com.command.polyshelf.presentation.screen.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.product.exception.ProductNetworkException
import com.command.polyshelf.domain.product.usecase.AddBarcodeCandidateProductUseCase
import com.command.polyshelf.domain.product.usecase.AddBarcodeProductUseCase
import com.command.polyshelf.domain.product.usecase.AddDataMatrixProductUseCase
import com.command.polyshelf.presentation.screen.scanner.analyzer.ScannedCodeResult
import com.command.polyshelf.presentation.screen.scanner.analyzer.ScannedCodeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ScannerViewModel(
    private val addDataMatrixProductUseCase: AddDataMatrixProductUseCase,
    private val addBarcodeProductUseCase: AddBarcodeProductUseCase,
    private val addBarcodeCandidateProductUseCase: AddBarcodeCandidateProductUseCase
) : ViewModel() {

    val state: StateFlow<ScannerState>
        field = MutableStateFlow(ScannerState())

    private var isProcessingScan = false

    fun onIntent(intent: ScannerIntent) {
        when (intent) {
            is ScannerIntent.CodeScanned -> handleCodeScanned(intent.result, intent.expirationDate)
            is ScannerIntent.SubmitCandidate -> handleSubmitCandidate(
                intent.code,
                intent.name,
                intent.expirationDate
            )

            is ScannerIntent.DismissCandidateDialog -> state.update { it.copy(candidateData = null) }
            is ScannerIntent.ClearError -> state.update { it.copy(networkErrorMessage = null) }
            is ScannerIntent.ResetState -> state.update { ScannerState() }
        }
    }

    private fun handleCodeScanned(result: ScannedCodeResult, expirationDate: String) {
        if (isProcessingScan || state.value.isLoading) return
        isProcessingScan = true

        viewModelScope.launch {
            try {
                state.update { it.copy(isLoading = true, networkErrorMessage = null) }

                val apiResult = if (result.type == ScannedCodeType.DATA_MATRIX) {
                    addDataMatrixProductUseCase(result.code)
                } else {
                    addBarcodeProductUseCase(result.code, expirationDate)
                }

                apiResult.onSuccess {
                    Timber.d("Product added successfully via scan")
                    state.update { it.copy(isLoading = false, isSuccess = true) }
                }.onFailure { ex ->
                    Timber.e(ex, "Failed to add scanned product")
                    if (ex is ProductNetworkException.ProductNotFound) {
                        state.update {
                            it.copy(
                                isLoading = false,
                                candidateData = CandidateDialogData(result.code, expirationDate)
                            )
                        }
                    } else {
                        val msg = if (ex is ProductNetworkException.NetworkError) {
                            "Отсутствует сеть или плохое соединение. Сканирование недоступно."
                        } else {
                            ex.message ?: "Не удалось добавить продукт. Проверьте сеть."
                        }
                        state.update { it.copy(isLoading = false, networkErrorMessage = msg) }
                    }
                }
            } finally {
                isProcessingScan = false
            }
        }
    }

    private fun handleSubmitCandidate(code: String, name: String, expirationDate: String) {
        viewModelScope.launch {
            state.update {
                it.copy(
                    isLoading = true,
                    candidateData = null,
                    networkErrorMessage = null
                )
            }

            val apiResult = addBarcodeCandidateProductUseCase(code, name, expirationDate)
            apiResult.onSuccess {
                Timber.d("Candidate product added successfully")
                state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { ex ->
                Timber.e(ex, "Failed to add candidate product")
                val msg = if (ex is ProductNetworkException.NetworkError) {
                    "Отсутствует сеть или плохое соединение."
                } else {
                    ex.message ?: "Ошибка при сохранении продукта."
                }
                state.update { it.copy(isLoading = false, networkErrorMessage = msg) }
            }
        }
    }
}

data class CandidateDialogData(
    val code: String,
    val defaultExpirationDate: String
)

data class ScannerState(
    val isLoading: Boolean = false,
    val networkErrorMessage: String? = null,
    val candidateData: CandidateDialogData? = null,
    val isSuccess: Boolean = false
)

sealed interface ScannerIntent {
    data class CodeScanned(
        val result: ScannedCodeResult,
        val expirationDate: String
    ) : ScannerIntent

    data class SubmitCandidate(
        val code: String,
        val name: String,
        val expirationDate: String
    ) : ScannerIntent

    data object DismissCandidateDialog : ScannerIntent
    data object ClearError : ScannerIntent
    data object ResetState : ScannerIntent
}
