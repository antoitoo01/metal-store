package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import java.math.BigDecimal
import java.util.UUID

data class LineResponse(
    val id: UUID,
    val invoiceId: UUID,
    val lineNumber: Int,
    val profileId: UUID?,
    val itemId: UUID?,
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal,
    val vatRate: BigDecimal,
    val totalPrice: BigDecimal
) {
    companion object {
        fun from(e: InvoiceLine) = LineResponse(
            id = e.id, invoiceId = e.invoiceId, lineNumber = e.lineNumber,
            profileId = e.profileId, itemId = e.itemId, description = e.description,
            quantity = e.quantity, unitPrice = e.unitPrice, vatRate = e.vatRate,
            totalPrice = e.totalPrice
        )
    }
}
