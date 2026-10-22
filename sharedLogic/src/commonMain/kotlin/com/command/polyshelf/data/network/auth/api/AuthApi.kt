package com.command.polyshelf.data.network.auth.api

import com.command.polyshelf.data.network.auth.dto.AuthResponse
import com.command.polyshelf.data.network.auth.dto.RefreshTokenRequest
import com.command.polyshelf.data.network.auth.dto.UserDto

interface AuthApi {
    suspend fun login(user: UserDto): Result<AuthResponse>
    suspend fun register(user: UserDto): Result<AuthResponse>
    suspend fun refresh(request: RefreshTokenRequest): Result<AuthResponse>
}
