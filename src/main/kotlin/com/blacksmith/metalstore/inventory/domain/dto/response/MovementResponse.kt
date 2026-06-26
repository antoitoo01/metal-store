package com.blacksmith.metalstore.inventory.domain.dto.response

import com.blacksmith.metalstore.inventory.domain.entity.MovementType
import com.blacksmith.metalstore.inventory.domain.entity.ReferenceType
import com.blacksmith.metalstore.inventory.domain.entity.StockMovement
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class MovementResponse(
    @field:Schema(description = "ID del movimiento")
    val id: UUID,

    @field:Schema(description = "ID del ítem de inventario")
    val inventoryItemId: UUID,

    @field:Schema(description = "Tipo de movimiento")
    val movementType: MovementType,

    @field:Schema(description = "Cantidad movida")
    val quantity: BigDecimal,

    @field:Schema(description = "Tipo de documento origen")
    val referenceType: ReferenceType?,

    @field:Schema(description = "ID del documento origen")
    val referenceId: UUID?,

    @field:Schema(description = "Cantidad anterior")
    val previousQuantity: BigDecimal,

    @field:Schema(description = "Cantidad nueva")
    val newQuantity: BigDecimal,

    @field:Schema(description = "Notas")
    val notes: String?,

    @field:Schema(description = "Fecha del movimiento")
    val performedAt: LocalDateTime
) {
    companion object {
        fun from(e: StockMovement) = MovementResponse(
            id = e.id,
            inventoryItemId = e.inventoryItemId,
            movementType = e.movementType,
            quantity = e.quantity,
            referenceType = e.referenceType,
            referenceId = e.referenceId,
            previousQuantity = e.previousQuantity,
            newQuantity = e.newQuantity,
            notes = e.notes,
            performedAt = e.performedAt
        )
    }
}
