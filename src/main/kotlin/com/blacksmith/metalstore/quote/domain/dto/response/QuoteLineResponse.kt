package com.blacksmith.metalstore.quote.domain.dto.response

import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import java.math.BigDecimal
import java.util.UUID

data class QuoteLineResponse(
    val id: UUID,
    val quoteId: UUID,
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
        fun from(e: QuoteLine) = QuoteLineResponse(
            id = e.id, quoteId = e.quoteId, lineNumber = e.lineNumber,
            profileId = e.profileId, itemId = e.itemId,
            description = e.description, quantity = e.quantity,
            unitPrice = e.unitPrice, vatRate = e.vatRate, totalPrice = e.totalPrice
        )
    }
}
