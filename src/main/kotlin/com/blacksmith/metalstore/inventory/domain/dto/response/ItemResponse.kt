package com.blacksmith.metalstore.inventory.domain.dto.response

import com.blacksmith.metalstore.inventory.domain.entity.InventoryItem
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class ItemResponse(
    @field:Schema(description = "Identificador único del ítem de inventario", example = "550e8400-e29b-41d4-a716-446655440000")
    val id: UUID,

    @field:Schema(description = "Identificador del inquilino", example = "550e8400-e29b-41d4-a716-446655440000")
    val tenantId: UUID,

    @field:Schema(description = "Identificador del perfil de artículo (opcional)", example = "550e8400-e29b-41d4-a716-446655440001")
    val profileId: UUID?,

    @field:Schema(description = "Identificador del artículo genérico (opcional)", example = "550e8400-e29b-41d4-a716-446655440002")
    val itemId: UUID?,

    @field:Schema(description = "Cantidad en inventario", example = "150.00")
    val quantity: BigDecimal,

    @field:Schema(description = "Ubicación en el almacén (opcional)", example = "Almacén A - Estante 12")
    val location: String?,

    @field:Schema(description = "Precio de costo en EUR (opcional)", example = "45.50")
    val costPriceEur: BigDecimal?,

    @field:Schema(description = "Proveedor del artículo (opcional)", example = "Aceros Industriales S.L.")
    val supplier: String?,

    @field:Schema(description = "Fecha de recepción en almacén", example = "2026-05-15T09:00:00")
    val receivedAt: LocalDateTime,

    @field:Schema(description = "Notas internas (opcional)", example = "Lote recibido el 15/05/2026")
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
