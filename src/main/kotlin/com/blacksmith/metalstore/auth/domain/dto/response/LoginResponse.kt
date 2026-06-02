package com.blacksmith.metalstore.auth.domain.dto.response

import com.blacksmith.metalstore.auth.domain.entity.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class LoginResponse(
    @field:Schema(description = "Token JWT de acceso", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6I...")
    val accessToken: String,

    @field:Schema(description = "Tipo de token", example = "Bearer")
    val tokenType: String = "Bearer",

    @field:Schema(description = "Token de renovación (opcional)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String? = null,

    @field:Schema(description = "Tiempo de expiración en segundos", example = "3600")
    val expiresIn: Int = 3600,

    @field:Schema(description = "Correo electrónico del usuario autenticado", example = "admin@metalstore.com")
    val email: String,

    @field:Schema(description = "Rol del usuario", example = "ADMIN")
    val role: Role,

    @field:Schema(description = "Identificador del inquilino", example = "550e8400-e29b-41d4-a716-446655440000")
    val tenantId: UUID,

    @field:Schema(description = "Nombre del inquilino/empresa", example = "MetalStore S.A.")
    val tenantName: String
)
