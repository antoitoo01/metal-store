package com.blacksmith.metalstore.auth.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
    @field:Schema(description = "Token de renovación (refresh token) emitido en el login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @field:NotBlank
    val refreshToken: String
)
