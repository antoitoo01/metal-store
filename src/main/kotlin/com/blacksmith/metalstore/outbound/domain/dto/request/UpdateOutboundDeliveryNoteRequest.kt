package com.blacksmith.metalstore.outbound.domain.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class UpdateOutboundDeliveryNoteRequest(
    @field:Schema(description = "ID del cliente", example = "550e8400-e29b-41d4-a716-446655440000")
    val customerId: UUID? = null,

    @field:Schema(description = "Nombre del cliente")
    val customerName: String? = null,

    @field:Schema(description = "NIF del cliente", example = "B12345678")
    val customerVat: String? = null,

    @field:Schema(description = "Dirección del cliente")
    val customerAddress: String? = null,

    @field:Schema(description = "Fecha del albarán", example = "2026-07-15")
    val issueDate: LocalDate? = null,

    @field:Schema(description = "Notas")
    val notes: String? = null
)
