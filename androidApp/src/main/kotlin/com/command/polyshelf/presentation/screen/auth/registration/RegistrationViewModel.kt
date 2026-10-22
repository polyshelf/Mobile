package com.command.polyshelf.presentation.screen.auth.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.domain.auth.exception.AuthUIException
import com.command.polyshelf.domain.auth.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    val state: StateFlow<RegistrationState>
        field = MutableStateFlow(RegistrationState())

    fun onIntent(intent: RegistrationIntent) {
        when (intent) {
            is RegistrationIntent.EmailChanged -> handleEmailChanged(intent.email)
            is RegistrationIntent.PasswordChanged -> handlePasswordChanged(intent.password)
            is RegistrationIntent.ConfirmPasswordChanged -> handleConfirmPasswordChanged(intent.confirmPassword)
            is RegistrationIntent.RegisterClicked -> handleRegisterClicked()
        }
    }

    private fun handleEmailChanged(email: String) {
        state.update { previousState ->
            previousState.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    private fun handlePasswordChanged(password: String) {
        state.update { previousState ->
            previousState.copy(
                password = password,
                errorMessage = null
            )
        }
    }

    private fun handleConfirmPasswordChanged(confirmPassword: String) {
        state.update { previousState ->
            previousState.copy(
                confirmPassword = confirmPassword,
                errorMessage = null
            )
        }
    }

    private fun handleRegisterClicked() {
        viewModelScope.launch {
            state.update { previousState ->
                previousState.copy(isLoading = true)
            }

            val email = state.value.email
            val password = state.value.password
            val confirmPassword = state.value.confirmPassword

            if (password != confirmPassword) {
                state.update { previousState ->
                    previousState.copy(
                        errorMessage = "Пароли не совпадают. Проверьте пароли ещё раз",
                        isLoading = false
                    )
                }
                return@launch
            }

            val result = registerUseCase(email, password)
            result.onFailure { ex ->
                val errorMessage = when (ex) {
                    is AuthNetworkException.UserWithThisEmailAlreadyExists -> "Email уже зарегистрирован"
                    is AuthNetworkException.InvalidEmailOrPasswordFormat -> "Неверный формат почты или пароля"
                    is AuthUIException.InvalidPassword -> "Пароль должен быть длиннее 6 символов"
                    is AuthUIException.EmptyEmail -> "Почта не может быть пустой"
                    is AuthUIException.InvalidEmailFormat -> "Неверный формат почты"
                    else -> ex.message ?: "Неизвестная ошибка"
                }
                state.update { previousState ->
                    previousState.copy(
                        errorMessage = errorMessage,
                        isLoading = false,
                        isSuccess = false
                    )
                }
            }.onSuccess {
                state.update { previousState ->
                    previousState.copy(
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true
                    )
                }
            }
        }
    }
}

sealed interface RegistrationIntent {
    data class EmailChanged(val email: String) : RegistrationIntent
    data class PasswordChanged(val password: String) : RegistrationIntent
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegistrationIntent
    data object RegisterClicked : RegistrationIntent
}

data class RegistrationState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
