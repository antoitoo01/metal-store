package com.blacksmith.metalstore.inventory.domain.dto.request

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class UpdateItemRequest(
    @field:Schema(description = "Nueva cantidad en inventario", example = "200.00")
    @field:Positive
    val quantity: BigDecimal,

    @field:Schema(description = "Ubicación en el almacén (opcional)", example = "Almacén B - Estante 5")
    val location: String? = null,

    @field:Schema(description = "Precio de costo en EUR (opcional)", example = "42.00")
    val costPriceEur: BigDecimal? = null,

    @field:Schema(description = "Proveedor del artículo (opcional)", example = "Hierros del Sur S.A.")
    val supplier: String? = null,

    @field:Schema(description = "Notas internas (opcional)", example = "Actualización de precio por nuevo lote")
    val notes: String? = null
) {
    fun toEntity(organizationId: UUID) = InventoryItem(
        organizationId = organizationId,
        quantity = quantity,
        location = location,
        costPriceEur = costPriceEur,
        supplier = supplier,
        notes = notes
    )
}
