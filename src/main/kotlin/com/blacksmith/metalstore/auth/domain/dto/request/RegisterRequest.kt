package com.blacksmith.metalstore.auth.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RegisterRequest(
    @field:Schema(description = "Nombre de usuario (opcional)", example = "jperez")
    val username: String?,

    @field:Schema(description = "Nombre del organizaci�n/empresa (opcional)", example = "Aceros del Norte S.A.")
    val organizationName: String?,

    @field:Schema(description = "Correo electrónico del usuario", example = "jperez@acerosdelnorte.com")
    @field:NotBlank
    @field:Email
    val email: String,

    @field:Schema(description = "Contraseña del usuario (mín. 8 caracteres, mayúscula, minúscula, número y especial)", example = "S3gur0!Pass")
    @field:NotBlank
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must be at least 8 characters with uppercase, lowercase, number, and special character"
    )
    val password: String
)
