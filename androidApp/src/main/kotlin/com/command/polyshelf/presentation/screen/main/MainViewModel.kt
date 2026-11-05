package com.command.polyshelf.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.command.polyshelf.domain.auth.usecase.LogoutUseCase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    fun logout() {
        viewModelScope.launch {
            Timber.d("MainViewModel: logout() called")
            logoutUseCase()
        }
    }
}
