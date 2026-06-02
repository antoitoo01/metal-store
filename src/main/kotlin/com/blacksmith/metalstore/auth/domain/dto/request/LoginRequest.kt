package com.blacksmith.metalstore.auth.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:Schema(description = "Correo electrónico del usuario", example = "admin@metalstore.com")
    @field:NotBlank
    @field:Email
    val email: String,

    @field:Schema(description = "Contraseña del usuario", example = "Str0ng!Pass")
    @field:NotBlank
    val password: String
)
