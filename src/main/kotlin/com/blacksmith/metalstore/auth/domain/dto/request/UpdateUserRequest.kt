package com.blacksmith.metalstore.auth.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UpdateUserRequest(
    @field:Schema(description = "Identificador único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Nombre de usuario (opcional)", example = "jperez")
    val username: String?,

    @field:Schema(description = "Nuevo correo electrónico del usuario", example = "jperez@acerosdelnorte.com")
    @field:NotBlank
    @field:Email
    val email: String
)
