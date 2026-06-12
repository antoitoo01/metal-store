package com.blacksmith.metalstore.client.domain.dto.request

import com.blacksmith.metalstore.client.domain.entity.Client
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class CreateClientRequest(
    @field:Schema(description = "Nombre del cliente", example = "Aceros del Norte S.A.")
    @field:NotBlank
    val name: String,

    @field:Schema(description = "Correo electrónico del cliente (opcional)", example = "contacto@acerosdelnorte.com")
    val email: String? = null,

    @field:Schema(description = "Teléfono del cliente (opcional)", example = "+54 11 5555-1234")
    val phone: String? = null,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val address: String? = null,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val vatNumber: String? = null,

    @field:Schema(description = "Notas internas sobre el cliente (opcional)", example = "Cliente habitual, pago a 60 días")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID) = Client(
        organizationId = organizationId,
        name = name,
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes
    )
}
