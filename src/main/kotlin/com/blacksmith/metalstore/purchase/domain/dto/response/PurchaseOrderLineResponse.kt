package com.blacksmith.metalstore.purchase.domain.dto.response

import com.blacksmith.metalstore.purchase.domain.entity.PurchaseOrderLine
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.UUID

data class PurchaseOrderLineResponse(
    @field:Schema(description = "ID de la línea")
    val id: UUID,

    @field:Schema(description = "ID de la orden de compra")
    val poId: UUID,

    @field:Schema(description = "Número de línea")
    val lineNumber: Int,

    @field:Schema(description = "ID del perfil del catálogo")
    val profileId: UUID?,

    @field:Schema(description = "ID del item del catálogo")
    val itemId: UUID?,

    @field:Schema(description = "Descripción")
    val description: String,

    @field:Schema(description = "Cantidad")
    val quantity: BigDecimal,

    @field:Schema(description = "Precio unitario (€)")
    val unitPrice: BigDecimal,

    @field:Schema(description = "Tipo de IVA (%)")
    val vatRate: BigDecimal,

    @field:Schema(description = "Importe total")
    val totalPrice: BigDecimal
) {
    companion object {
        fun from(e: PurchaseOrderLine) = PurchaseOrderLineResponse(
            id = e.id,
            poId = e.poId,
            lineNumber = e.lineNumber,
            profileId = e.profileId,
            itemId = e.itemId,
            description = e.description,
            quantity = e.quantity,
            unitPrice = e.unitPrice,
            vatRate = e.vatRate,
            totalPrice = e.totalPrice
        )
    }
}
