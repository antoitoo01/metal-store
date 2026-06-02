package com.blacksmith.metalstore.auth.domain.dto.response

import com.blacksmith.metalstore.auth.domain.entity.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UserResponse(
    @field:Schema(description = "Identificador único del usuario", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Nombre de usuario", example = "jperez")
    val username: String,

    @field:Schema(description = "Correo electrónico del usuario", example = "jperez@acerosdelnorte.com")
    val email: String,

    @field:Schema(description = "Rol del usuario", example = "ADMIN")
    val role: Role,

    @field:Schema(description = "Identificador del inquilino", example = "550e8400-e29b-41d4-a716-446655440000")
    val tenantId: UUID,

    @field:Schema(description = "Nombre del inquilino/empresa", example = "Aceros del Norte S.A.")
    val tenantName: String
)
