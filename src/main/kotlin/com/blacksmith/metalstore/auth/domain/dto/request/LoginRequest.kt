package com.blacksmith.metalstore.auth.domain.dto.request

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String
)
