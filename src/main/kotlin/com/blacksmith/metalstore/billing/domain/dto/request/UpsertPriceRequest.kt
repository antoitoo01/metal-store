package com.blacksmith.metalstore.billing.domain.dto.request

import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class UpsertPriceRequest(
    val profileId: UUID? = null,
    val itemId: UUID? = null,

    @field:Positive
    val unitPrice: BigDecimal,

    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val notes: String? = null
) {
    fun toEntity(tenantId: UUID) = PriceListItem(
        tenantId = tenantId,
        profileId = profileId,
        itemId = itemId,
        unitPrice = unitPrice,
        validFrom = validFrom,
        validTo = validTo,
        notes = notes
    )
}
