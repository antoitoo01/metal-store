package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class InvoiceResponse(
    @field:Schema(description = "Identificador único de la factura", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del organizaci�n", example = "550e8400-e29b-41d4-a716-446655440000")
    val organizationId: UUID,

    @field:Schema(description = "Número de factura", example = "FAC-2026-00123")
    val invoiceNumber: String,

    @field:Schema(description = "Nombre del cliente (opcional)", example = "Aceros del Norte S.A.")
    val customerName: String?,

    @field:Schema(description = "CIF/NIF del cliente (opcional)", example = "B-12345678")
    val customerVat: String?,

    @field:Schema(description = "Dirección del cliente (opcional)", example = "Av. Industrial 1234, Buenos Aires")
    val customerAddress: String?,

    @field:Schema(description = "Fecha de emisión", example = "2026-05-15")
    val issueDate: LocalDate,

    @field:Schema(description = "Fecha de vencimiento (opcional)", example = "2026-06-15")
    val dueDate: LocalDate?,

    @field:Schema(description = "Estado de la factura", example = "ISSUED")
    val status: InvoiceStatus,

    @field:Schema(description = "Subtotal sin IVA", example = "1250.00")
    val subtotal: BigDecimal,

    @field:Schema(description = "Total de IVA", example = "262.50")
    val vatTotal: BigDecimal,

    @field:Schema(description = "Total con IVA", example = "1512.50")
    val total: BigDecimal,

    @field:Schema(description = "Notas internas (opcional)", example = "Factura correspondiente a mayo 2026")
    val notes: String?
) {
    companion object {
        fun from(e: Invoice) = InvoiceResponse(
            id = e.id, organizationId = e.organizationId, invoiceNumber = e.invoiceNumber,
            customerName = e.customerName, customerVat = e.customerVat,
            customerAddress = e.customerAddress, issueDate = e.issueDate,
            dueDate = e.dueDate, status = e.status, subtotal = e.subtotal,
            vatTotal = e.vatTotal, total = e.total, notes = e.notes
        )
    }
}
