package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.Invoice
import com.blacksmith.metalstore.billing.domain.entity.InvoiceStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class InvoiceResponse(
    val id: UUID,
    val tenantId: UUID,
    val invoiceNumber: String,
    val customerName: String?,
    val customerVat: String?,
    val customerAddress: String?,
    val issueDate: LocalDate,
    val dueDate: LocalDate?,
    val status: InvoiceStatus,
    val subtotal: BigDecimal,
    val vatTotal: BigDecimal,
    val total: BigDecimal,
    val notes: String?
) {
    companion object {
        fun from(e: Invoice) = InvoiceResponse(
            id = e.id, tenantId = e.tenantId, invoiceNumber = e.invoiceNumber,
            customerName = e.customerName, customerVat = e.customerVat,
            customerAddress = e.customerAddress, issueDate = e.issueDate,
            dueDate = e.dueDate, status = e.status, subtotal = e.subtotal,
            vatTotal = e.vatTotal, total = e.total, notes = e.notes
        )
    }
}
