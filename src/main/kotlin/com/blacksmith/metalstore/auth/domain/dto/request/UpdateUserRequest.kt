package com.blacksmith.metalstore.auth.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email

data class UpdateUserRequest(
    @field:Schema(description = "Nombre de usuario (opcional)", example = "jperez")
    val username: String?,

    @field:Schema(description = "Nuevo correo electrónico del usuario", example = "jperez@acerosdelnorte.com")
    @field:Email
    val email: String?
)
