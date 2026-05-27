package com.blacksmith.metalstore.billing.domain.dto.response

import com.blacksmith.metalstore.billing.domain.entity.PriceListItem
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class PriceResponse(
    val id: UUID,
    val tenantId: UUID,
    val profileId: UUID?,
    val itemId: UUID?,
    val unitPrice: BigDecimal,
    val validFrom: LocalDate?,
    val validTo: LocalDate?,
    val notes: String?
) {
    companion object {
        fun from(e: PriceListItem) = PriceResponse(
            id = e.id, tenantId = e.tenantId, profileId = e.profileId, itemId = e.itemId,
            unitPrice = e.unitPrice, validFrom = e.validFrom, validTo = e.validTo, notes = e.notes
        )
    }
}
