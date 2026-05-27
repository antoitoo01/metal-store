package com.blacksmith.metalstore.billing.domain.dto.request

import com.blacksmith.metalstore.billing.domain.entity.InvoiceLine
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreateLineRequest(
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
    fun toEntity(invoiceId: UUID) = InvoiceLine(
        invoiceId = invoiceId,
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
