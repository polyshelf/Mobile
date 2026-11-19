package com.command.polyshelf.presentation.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.domain.auth.usecase.CheckAuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashViewModel(
    private val checkAuthUseCase: CheckAuthUseCase
) : ViewModel() {

    val authState: StateFlow<SplashAuthState>
        field = MutableStateFlow<SplashAuthState>(SplashAuthState.Loading)

    init {
        checkAuth()
    }

    fun checkAuth() {
        viewModelScope.launch {
            Timber.d("SplashViewModel: checkAuth() started")
            authState.update { SplashAuthState.Loading }
            val result = checkAuthUseCase()
            result.onSuccess {
                Timber.d("SplashViewModel: checkAuth SUCCESS -> SplashAuthState.Authenticated")
                authState.update { SplashAuthState.Authenticated }
            }.onFailure { ex ->
                Timber.e(ex, "SplashViewModel: checkAuth FAILED -> SplashAuthState.Unauthenticated. Reason: ${ex.message}")
                authState.update { SplashAuthState.Unauthenticated }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Timber.d("SplashViewModel: logout() called")
            checkAuthUseCase.logout()
        }
    }
}

sealed interface SplashAuthState {
    data object Loading : SplashAuthState
    data object Authenticated : SplashAuthState
    data object Unauthenticated : SplashAuthState
}
