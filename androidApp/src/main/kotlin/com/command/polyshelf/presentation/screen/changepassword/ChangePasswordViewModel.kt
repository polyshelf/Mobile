package com.command.polyshelf.presentation.screen.changepassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.domain.auth.exception.AuthUIException
import com.command.polyshelf.domain.auth.usecase.ChangePasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class ChangePasswordViewModel(
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    val state: StateFlow<ChangePasswordState>
        field = MutableStateFlow(ChangePasswordState())

    fun onIntent(intent: ChangePasswordIntent) {
        when (intent) {
            is ChangePasswordIntent.CurrentPasswordChanged -> state.update {
                it.copy(
                    currentPassword = intent.value,
                    errorMessage = null,
                    successMessage = null
                )
            }

            is ChangePasswordIntent.NewPasswordChanged -> state.update {
                it.copy(
                    newPassword = intent.value,
                    errorMessage = null,
                    successMessage = null
                )
            }

            is ChangePasswordIntent.ConfirmPasswordChanged -> state.update {
                it.copy(
                    confirmPassword = intent.value,
                    errorMessage = null,
                    successMessage = null
                )
            }

            is ChangePasswordIntent.Submit -> handleSubmit()
            is ChangePasswordIntent.ClearMessages -> state.update {
                it.copy(
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    private fun handleSubmit() {
        val s = state.value
        viewModelScope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = changePasswordUseCase(s.currentPassword, s.newPassword, s.confirmPassword)
            if (result.isSuccess) {
                state.update {
                    it.copy(
                        isLoading = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Пароль успешно обновлён!"
                    )
                }
            } else {
                val ex = result.exceptionOrNull()
                Timber.e(ex, "Failed to change password")
                val msg = when (ex) {
                    is AuthUIException.InvalidPassword -> "Пароль должен быть длиннее 6 символов"
                    is AuthNetworkException.InvalidLinkPassword -> "Неверный текущий пароль"
                    is AuthNetworkException.InvalidEmailOrPasswordFormat -> "Неверный формат пароля"
                    else -> ex?.message ?: "Не удалось сменить пароль"
                }
                state.update { it.copy(isLoading = false, errorMessage = msg) }
            }
        }
    }
}

data class ChangePasswordState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface ChangePasswordIntent {
    data class CurrentPasswordChanged(val value: String) : ChangePasswordIntent
    data class NewPasswordChanged(val value: String) : ChangePasswordIntent
    data class ConfirmPasswordChanged(val value: String) : ChangePasswordIntent
    data object Submit : ChangePasswordIntent
    data object ClearMessages : ChangePasswordIntent
}
