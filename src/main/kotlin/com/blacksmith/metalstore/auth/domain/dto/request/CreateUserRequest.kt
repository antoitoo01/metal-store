package com.blacksmith.metalstore.auth.domain.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    val username: String?,

    @field:NotBlank
    @field:Email
    val email: String
)
