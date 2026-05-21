package com.blacksmith.metalstore.auth.domain.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class CreateUserRequest(

    val username: String?,

    @NotNull
    @Email
    val email: String,

    @NotNull
    val password: String
)
