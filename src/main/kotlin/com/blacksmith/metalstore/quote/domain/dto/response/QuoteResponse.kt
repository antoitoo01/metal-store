package com.blacksmith.metalstore.quote.domain.dto.response

import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class QuoteResponse(
    @field:Schema(description = "Identificador único del presupuesto", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del organizaci�n", example = "550e8400-e29b-41d4-a716-446655440000")
    val organizationId: UUID,

    @field:Schema(description = "Número de presupuesto", example = "PRE-2026-00042")
    val quoteNumber: String,

    @field:Schema(description = "Identificador del cliente (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val clientId: UUID?,

    @field:Schema(description = "Nombre del cliente (opcional)", example = "Aceros del Norte S.A.")
    val customerName: String?,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val customerVat: String?,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val customerAddress: String?,

    @field:Schema(description = "Fecha de emisión", example = "2026-05-15")
    val issueDate: LocalDate,

    @field:Schema(description = "Fecha de validez (opcional)", example = "2026-06-15")
    val validUntil: LocalDate?,

    @field:Schema(description = "Estado del presupuesto", example = "PENDING")
    val status: QuoteStatus,

    @field:Schema(description = "Subtotal sin IVA", example = "2500.00")
    val subtotal: BigDecimal,

    @field:Schema(description = "Total de IVA", example = "525.00")
    val vatTotal: BigDecimal,

    @field:Schema(description = "Total con IVA", example = "3025.00")
    val total: BigDecimal,

    @field:Schema(description = "Notas internas (opcional)", example = "Presupuesto para obra en construcción")
    val notes: String?
) {
    companion object {
        fun from(e: Quote) = QuoteResponse(
            id = e.id, organizationId = e.organizationId, quoteNumber = e.quoteNumber,
            clientId = e.clientId, customerName = e.customerName,
            customerVat = e.customerVat, customerAddress = e.customerAddress,
            issueDate = e.issueDate, validUntil = e.validUntil,
            status = e.status, subtotal = e.subtotal, vatTotal = e.vatTotal,
            total = e.total, notes = e.notes
        )
    }
}
