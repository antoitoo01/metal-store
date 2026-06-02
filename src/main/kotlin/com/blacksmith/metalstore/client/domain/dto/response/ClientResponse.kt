package com.blacksmith.metalstore.client.domain.dto.response

import com.blacksmith.metalstore.client.domain.entity.Client
import com.blacksmith.metalstore.client.domain.entity.ClientStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.util.UUID

data class ClientResponse(
    @field:Schema(description = "Identificador único del cliente", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del inquilino", example = "550e8400-e29b-41d4-a716-446655440000")
    val tenantId: UUID,

    @field:Schema(description = "Nombre del cliente", example = "Aceros del Norte S.A.")
    val name: String,

    @field:Schema(description = "Correo electrónico del cliente (opcional)", example = "contacto@acerosdelnorte.com")
    val email: String?,

    @field:Schema(description = "Teléfono del cliente (opcional)", example = "+54 11 5555-1234")
    val phone: String?,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val address: String?,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val vatNumber: String?,

    @field:Schema(description = "Notas internas (opcional)", example = "Cliente habitual, pago a 60 días")
    val notes: String?,

    @field:Schema(description = "Estado del cliente", example = "ACTIVE")
    val status: ClientStatus,

    @field:Schema(description = "Fecha de creación", example = "2026-01-15T10:30:00")
    val createdAt: LocalDateTime,

    @field:Schema(description = "Fecha de última modificación", example = "2026-05-20T14:45:00")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(e: Client) = ClientResponse(
            id = e.id, tenantId = e.tenantId, name = e.name, email = e.email,
            phone = e.phone, address = e.address, vatNumber = e.vatNumber,
            notes = e.notes, status = e.status, createdAt = e.createdAt,
            updatedAt = e.updatedAt
        )
    }
}
