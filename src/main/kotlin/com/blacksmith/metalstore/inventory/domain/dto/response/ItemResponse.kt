package com.blacksmith.metalstore.inventory.domain.dto.response

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ItemResponse(
    val id: UUID,
    val tenantId: UUID,
    val profileId: UUID?,
    val itemId: UUID?,
    val quantity: BigDecimal,
    val location: String?,
    val costPriceEur: BigDecimal?,
    val supplier: String?,
    val receivedAt: LocalDateTime,
    val notes: String?
) {
    companion object {
        fun from(e: InventoryItem) = ItemResponse(
            id = e.id, tenantId = e.tenantId, profileId = e.profileId, itemId = e.itemId,
            quantity = e.quantity, location = e.location, costPriceEur = e.costPriceEur,
            supplier = e.supplier, receivedAt = e.receivedAt, notes = e.notes
        )
    }
}
