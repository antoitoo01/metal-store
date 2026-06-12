package com.blacksmith.metalstore.quote.domain.dto.request

import com.blacksmith.metalstore.quote.domain.entity.Quote
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class CreateQuoteRequest(
    @field:Schema(description = "Identificador del cliente (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    val clientId: UUID? = null,

    @field:Schema(description = "Nombre del cliente (opcional)", example = "Aceros del Norte S.A.")
    val customerName: String? = null,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val customerVat: String? = null,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val customerAddress: String? = null,

    @field:Schema(description = "Fecha de validez del presupuesto (opcional)", example = "2026-07-15")
    val validUntil: LocalDate? = null,

    @field:Schema(description = "Notas internas del presupuesto (opcional)", example = "Presupuesto válido por 30 días")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID, quoteNumber: String) = Quote(
        organizationId = organizationId,
        quoteNumber = quoteNumber,
        clientId = clientId,
        customerName = customerName,
        customerVat = customerVat,
        customerAddress = customerAddress,
        validUntil = validUntil,
        notes = notes
    )
}
