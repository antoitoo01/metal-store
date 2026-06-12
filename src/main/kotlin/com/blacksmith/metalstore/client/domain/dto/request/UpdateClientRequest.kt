package com.blacksmith.metalstore.client.domain.dto.request

import com.blacksmith.metalstore.client.domain.entity.Client
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class UpdateClientRequest(
    @field:Schema(description = "Nombre del cliente (opcional)", example = "Aceros del Norte S.A.")
    val name: String? = null,

    @field:Schema(description = "Correo electrónico del cliente (opcional)", example = "contacto@acerosdelnorte.com")
    val email: String? = null,

    @field:Schema(description = "Teléfono del cliente (opcional)", example = "+54 11 5555-1234")
    val phone: String? = null,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val address: String? = null,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val vatNumber: String? = null,

    @field:Schema(description = "Notas internas (opcional)", example = "Cliente habitual, pago a 60 días")
    val notes: String? = null,

    @field:Schema(description = "Estado del cliente (opcional)", example = "ACTIVE")
    val status: String? = null
) {
    fun toEntity(organizationId: UUID) = Client(
        organizationId = organizationId,
        name = name ?: "",
        email = email,
        phone = phone,
        address = address,
        vatNumber = vatNumber,
        notes = notes,
        status = status?.let { com.blacksmith.metalstore.client.domain.entity.ClientStatus.valueOf(it) }
            ?: com.blacksmith.metalstore.client.domain.entity.ClientStatus.ACTIVE
    )
}
