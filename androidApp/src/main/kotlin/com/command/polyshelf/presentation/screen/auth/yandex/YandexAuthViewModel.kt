package com.command.polyshelf.presentation.screen.auth.yandex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.data.network.auth.exception.AuthNetworkException
import com.command.polyshelf.domain.auth.usecase.YandexLoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class YandexAuthViewModel(
    private val yandexLoginUseCase: YandexLoginUseCase
) : ViewModel() {

    val state: StateFlow<YandexAuthState>
        field = MutableStateFlow(YandexAuthState())

    private var pendingToken: String? = null

    fun onIntent(intent: YandexAuthIntent) {
        when (intent) {
            is YandexAuthIntent.TokenReceived -> signIn(intent.token, linkPassword = null)
            is YandexAuthIntent.SdkFailed -> state.update {
                it.copy(isLoading = false, errorMessage = "Не удалось войти через Яндекс")
            }
            is YandexAuthIntent.LinkPasswordChanged -> state.update {
                it.copy(link = it.link?.copy(password = intent.password, error = null))
            }
            is YandexAuthIntent.LinkConfirmed -> {
                val token = pendingToken
                val password = state.value.link?.password
                if (token != null && !password.isNullOrEmpty()) signIn(token, password)
            }
            is YandexAuthIntent.LinkDismissed -> {
                pendingToken = null
                state.update { it.copy(link = null) }
            }
        }
    }

    private fun signIn(token: String, linkPassword: String?) {
        viewModelScope.launch {
            state.update { it.copy(isLoading = true, errorMessage = null) }
            yandexLoginUseCase(token, linkPassword)
                .onSuccess {
                    pendingToken = null
                    state.update { it.copy(isLoading = false, link = null, isSuccess = true) }
                }
                .onFailure { ex -> handleFailure(token, ex) }
        }
    }

    private fun handleFailure(token: String, ex: Throwable) {
        when (ex) {
            is AuthNetworkException.YandexLinkRequired -> {
                pendingToken = token
                state.update {
                    it.copy(
                        isLoading = false,
                        link = LinkState(
                            message = ex.message
                                ?: "Аккаунт с этой почтой уже есть. Привязать к нему Яндекс?"
                        )
                    )
                }
            }
            is AuthNetworkException.InvalidLinkPassword -> state.update {
                it.copy(isLoading = false, link = it.link?.copy(error = "Неверный пароль"))
            }
            else -> state.update {
                it.copy(isLoading = false, errorMessage = messageFor(ex))
            }
        }
    }

    private fun messageFor(ex: Throwable): String = when (ex) {
        is AuthNetworkException.YandexTokenInvalid -> "Яндекс не подтвердил вход. Попробуйте ещё раз"
        is AuthNetworkException.UserWithThisEmailAlreadyExists ->
            "Эта почта уже привязана к другому Яндекс-аккаунту"
        is AuthNetworkException.ServerError -> "Сервер временно недоступен. Попробуйте позже"
        else -> ex.message ?: "Неизвестная ошибка"
    }
}

sealed interface YandexAuthIntent {
    data class TokenReceived(val token: String) : YandexAuthIntent
    data object SdkFailed : YandexAuthIntent
    data class LinkPasswordChanged(val password: String) : YandexAuthIntent
    data object LinkConfirmed : YandexAuthIntent
    data object LinkDismissed : YandexAuthIntent
}

data class LinkState(
    val message: String,
    val password: String = "",
    val error: String? = null
)

data class YandexAuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val link: LinkState? = null
)
