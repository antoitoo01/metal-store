package com.blacksmith.metalstore.inventory.domain.dto.request

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class UpdateItemRequest(
    @field:Positive
    val quantity: BigDecimal,

    val location: String? = null,

    val costPriceEur: BigDecimal? = null,

    val supplier: String? = null,

    val notes: String? = null
) {
    fun toEntity(tenantId: UUID) = InventoryItem(
        tenantId = tenantId,
        quantity = quantity,
        location = location,
        costPriceEur = costPriceEur,
        supplier = supplier,
        notes = notes
    )
}
