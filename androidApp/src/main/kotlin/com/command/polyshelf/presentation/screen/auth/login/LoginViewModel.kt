package com.command.polyshelf.presentation.screen.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.domain.auth.exception.AuthUIException
import com.command.polyshelf.domain.auth.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    val state: StateFlow<LoginState>
        field = MutableStateFlow(LoginState())

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> handleEmailChanged(intent.email)
            is LoginIntent.PasswordChanged -> handlePasswordChanged(intent.password)
            is LoginIntent.LoginClicked -> handleLoginClicked()
        }
    }

    private fun handleEmailChanged(email: String) {
        state.update { previousValue ->
            previousValue.copy(email = email, errorMessage = null)
        }
    }

    private fun handlePasswordChanged(password: String) {
        state.update { previousValue ->
            previousValue.copy(password = password, errorMessage = null)
        }
    }

    private fun handleLoginClicked() {
        viewModelScope.launch {
            state.update { previousValue ->
                previousValue.copy(isLoading = true, errorMessage = null)
            }

            val email = state.value.email
            val password = state.value.password
            val result = loginUseCase(email, password)

            result.onFailure { ex ->
                Timber.d("Ошибка авторизации: ${ex.cause}")
                val errorMsg = when (ex) {
                    is AuthNetworkException.InvalidCredentials -> "Неверный email или пароль"
                    is AuthNetworkException.InvalidEmailOrPasswordFormat -> "Неверный формат почты или пароля"
                    is AuthUIException.EmptyEmail -> "Почта не может быть пустой"
                    is AuthUIException.InvalidEmailFormat -> "Неверный формат почты"
                    is AuthUIException.InvalidPassword -> "Пароль должен быть длиннее 6 символов"
                    else -> ex.message ?: "Неизвестная ошибка"
                }
                state.update { previousValue ->
                    previousValue.copy(
                        isLoading = false,
                        errorMessage = errorMsg,
                        isSuccess = false
                    )
                }
            }.onSuccess {
                state.update { previousValue ->
                    previousValue.copy(
                        isLoading = false,
                        errorMessage = null,
                        isSuccess = true
                    )
                }
            }
        }
    }
}

sealed interface LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data object LoginClicked : LoginIntent
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
