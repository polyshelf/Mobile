package com.command.polyshelf.domain.auth.mapper

import com.command.polyshelf.data.network.auth.dto.UserDto
import com.command.polyshelf.domain.auth.model.User

fun User.toDto() =
    UserDto(
        email = email,
        password = password
    )
