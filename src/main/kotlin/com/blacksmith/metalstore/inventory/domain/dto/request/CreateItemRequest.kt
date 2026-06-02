package com.blacksmith.metalstore.inventory.domain.dto.request

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.util.UUID

data class CreateItemRequest(
    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440000")
    val profileId: UUID? = null,

    @field:Schema(description = "Identificador del artículo genérico (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val itemId: UUID? = null,

    @field:Schema(description = "Cantidad en inventario", example = "150.00")
    @field:Positive
    val quantity: BigDecimal,

    @field:Schema(description = "Ubicación en el almacén (opcional)", example = "Almacén A - Estante 12")
    val location: String? = null,

    @field:Schema(description = "Precio de costo en EUR (opcional)", example = "45.50")
    val costPriceEur: BigDecimal? = null,

    @field:Schema(description = "Proveedor del artículo (opcional)", example = "Aceros Industriales S.L.")
    val supplier: String? = null,

    @field:Schema(description = "Notas internas (opcional)", example = "Lote recibido el 15/05/2026")
    val notes: String? = null
) {
    fun toEntity(tenantId: UUID) = InventoryItem(
        tenantId = tenantId,
        profileId = profileId,
        itemId = itemId,
        quantity = quantity,
        location = location,
        costPriceEur = costPriceEur,
        supplier = supplier,
        notes = notes
    )
}
