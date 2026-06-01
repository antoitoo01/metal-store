package com.blacksmith.metalstore.quote.domain.dto.response

import com.blacksmith.metalstore.quote.domain.entity.Quote
import com.blacksmith.metalstore.quote.domain.entity.QuoteStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class QuoteResponse(
    val id: UUID,
    val tenantId: UUID,
    val quoteNumber: String,
    val clientId: UUID?,
    val customerName: String?,
    val customerVat: String?,
    val customerAddress: String?,
    val issueDate: LocalDate,
    val validUntil: LocalDate?,
    val status: QuoteStatus,
    val subtotal: BigDecimal,
    val vatTotal: BigDecimal,
    val total: BigDecimal,
    val notes: String?
) {
    companion object {
        fun from(e: Quote) = QuoteResponse(
            id = e.id, tenantId = e.tenantId, quoteNumber = e.quoteNumber,
            clientId = e.clientId, customerName = e.customerName,
            customerVat = e.customerVat, customerAddress = e.customerAddress,
            issueDate = e.issueDate, validUntil = e.validUntil,
            status = e.status, subtotal = e.subtotal, vatTotal = e.vatTotal,
            total = e.total, notes = e.notes
        )
    }
}
