package com.blacksmith.metalstore.quote.domain.dto.request

import com.blacksmith.metalstore.quote.domain.entity.QuoteLine
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreateQuoteLineRequest(
    val lineNumber: Int,
    val profileId: UUID? = null,
    val itemId: UUID? = null,
    val description: String,
    @field:Positive
    val quantity: BigDecimal,
    @field:Positive
    val unitPrice: BigDecimal,
    val vatRate: BigDecimal = BigDecimal("21.00")
) {
    fun toEntity(quoteId: UUID) = QuoteLine(
        quoteId = quoteId,
        lineNumber = lineNumber,
        profileId = profileId,
        itemId = itemId,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice,
        vatRate = vatRate,
        totalPrice = BigDecimal.ZERO
    )
}
